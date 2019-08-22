package cn.autolabor.module.networkhub;

import cn.autolabor.core.annotation.FilterMessage;
import cn.autolabor.core.annotation.TaskFunction;
import cn.autolabor.core.annotation.TaskParameter;
import cn.autolabor.core.annotation.TaskProperties;
import cn.autolabor.core.server.ServerManager;
import cn.autolabor.core.server.executor.AbstractTask;
import cn.autolabor.core.server.message.MessageHandle;
import cn.autolabor.core.server.message.MessageSource;
import cn.autolabor.core.server.message.MessageSourceType;
import cn.autolabor.module.networkhub.remote.modules.multicast.MulticastBroadcaster;
import cn.autolabor.module.networkhub.remote.modules.multicast.PacketSlicer;
import cn.autolabor.module.networkhub.remote.resources.MulticastSockets;
import cn.autolabor.module.networkhub.remote.resources.Name;
import cn.autolabor.module.networkhub.remote.resources.Networks;
import cn.autolabor.module.networkhub.remote.resources.UdpCmd;
import cn.autolabor.util.Sugar;
import cn.autolabor.util.autobuf.AutoBufAdapter;
import cn.autolabor.util.autobuf.ByteBuilder;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@TaskProperties(name = "UDPMulticastBroadcaster")
public class UDPMulticastBroadcaster extends AbstractTask {

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    private final Networks networks;                  // 本机网络端口资源
    private final MulticastSockets sockets;           // 组播套接字资源
    private final MulticastBroadcaster broadcaster;   // 组播广播装置
    @TaskParameter(name = "networkInterfaceName", value = "")
    private String networkInterfaceName;
    @TaskParameter(name = "ip", value = "238.88.08.07")
    private String ip;
    @TaskParameter(name = "port", value = "30000")
    private int port;
    @TaskParameter(name = "threadCount", value = "1")
    private int threadCount;
    @TaskParameter(name = "sendHeader", value = "false")
    private boolean sendHeader;
    @TaskParameter(name = "slicerSize", value = "5000")
    private int slicerSize;
    private ConcurrentHashMap<String, AutoBufAdapter> adapterPool = new ConcurrentHashMap<>();
    private List<UDPSendTask> sendTasks = new ArrayList<>();
    private Random rand = new Random();

    public UDPMulticastBroadcaster(String... name) {
        super(name);
        networks = RemoteHub.ME.setAndGet(new Networks());
        sockets = RemoteHub.ME.setAndGet(new MulticastSockets(new InetSocketAddress(ip, port)));
        sockets.get(Sugar.tryGetNetworkInterface(networkInterfaceName));
        broadcaster = RemoteHub.ME.setAndGet(new MulticastBroadcaster(slicerSize));
        RemoteHub.ME.setAndGet(new PacketSlicer());
        RemoteHub.ME.setAndGet(new Name(ServerManager.getIdentification()));

        for (int i = 0; i < threadCount; i++) {
            sendTasks.add(ServerManager.me().register(new UDPSendTask()));
        }
    }

    /***
     *  绑定组播的话题
     */
    @FilterMessage
    @TaskFunction(name = "bindTopic")
    public void bindTopic(MessageHandle handle) {
        if (!handle.getTopic().startsWith("_")) {
            adapterPool.put(handle.getTopic(), new AutoBufAdapter(handle.getDataType()));
            handle.addCallback(this, "distributeMessage", new MessageSourceType[]{MessageSourceType.RAM});
        }
    }

    /***
     *  收到话题数据，随机选取“组播发送”任务，发送消息
     */
    @TaskFunction(name = "distributeMessage")
    public void distributeMessage(Object msg, MessageSource source) {
        String topic = source.getTopic();
        UDPSendTask udpSendTask = sendTasks.get(rand.nextInt(threadCount));
        ServerManager.me().run(udpSendTask, "send", topic, msg, adapterPool.get(topic));
    }


    @TaskProperties(name = "UDPMulticastBroadcaster::Send", unique = false)
    public class UDPSendTask extends AbstractTask {

        UDPSendTask(String... name) {
            super(name);
        }

        /***
         * MESSAGE:
         * | TOPIC (String with end 0x00) | MSG_ENCODE (n bytes) |
         */
        @TaskFunction(name = "send")
        public void send(String topic, Object msg, AutoBufAdapter adapter) {
            byte[] payload;
            if (sendHeader) {
                payload = new ByteBuilder().putStringWithTag(topic).putBytes(adapter.encode(msg)).toBytes();
            } else {
                payload = new ByteBuilder().putStringWithTag(topic).putBytes(adapter.encodeBody(msg)).toBytes();
            }
            broadcaster.broadcast(UdpCmd.TOPIC.id, payload);
        }
    }
}
