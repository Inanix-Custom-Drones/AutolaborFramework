package cn.autolabor.module.networkhub;

import cn.autolabor.core.annotation.TaskFunction;
import cn.autolabor.core.annotation.TaskParameter;
import cn.autolabor.core.annotation.TaskProperties;
import cn.autolabor.core.server.ServerManager;
import cn.autolabor.core.server.executor.AbstractTask;
import cn.autolabor.core.server.message.MessageHandle;
import cn.autolabor.core.server.message.MessageSource;
import cn.autolabor.core.server.message.MessageSourceType;
import cn.autolabor.module.networkhub.dependency.AbstractDependent;
import cn.autolabor.module.networkhub.remote.modules.multicast.MulticastListener;
import cn.autolabor.module.networkhub.remote.modules.multicast.MulticastReceiver;
import cn.autolabor.module.networkhub.remote.modules.multicast.PacketSlicer;
import cn.autolabor.module.networkhub.remote.resources.MulticastSockets;
import cn.autolabor.module.networkhub.remote.resources.Name;
import cn.autolabor.module.networkhub.remote.resources.Networks;
import cn.autolabor.module.networkhub.remote.resources.UdpCmd;
import cn.autolabor.util.autobuf.AutoBufAdapter;
import cn.autolabor.util.autobuf.ByteBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@TaskProperties(name = "UDPMulticastReceiver")
public class UDPMulticastReceiver extends AbstractTask {

    private final Networks networks;                      // 本机网络端口资源
    private final MulticastSockets sockets;               // 组播套接字资源
    private final MulticastReceiver receiver;             // 组播接收装置
    private final PacketSlicer slicer;

    @TaskParameter(name = "ip", value = "238.88.08.07")
    private String ip;
    @TaskParameter(name = "port", value = "30000")
    private int port;
    @TaskParameter(name = "threadCount", value = "1")
    private int threadCount;
    @TaskParameter(name = "refreshTimeout", value = "3000")
    private int refreshTimeout;
    @TaskParameter(name = "sendHeader", value = "false")
    private boolean sendHeader;


    private ConcurrentHashMap<String, AutoBufAdapter> adapterPool = new ConcurrentHashMap<>();
    private List<UDPReceiveTask> receiveTasks = new ArrayList<>();
    private Random rand = new Random();

    public UDPMulticastReceiver(String... name) {
        super(name);
        networks = RemoteHub.ME.setAndGet(new Networks());
        sockets = RemoteHub.ME.setAndGet(new MulticastSockets(new InetSocketAddress(ip, port)));
        receiver = RemoteHub.ME.setAndGet(new MulticastReceiver());
        slicer = RemoteHub.ME.setAndGet(new PacketSlicer());
        RemoteHub.ME.setAndGet(new Name(ServerManager.getIdentification()));
        RemoteHub.ME.setAndGet(new ReceiverMulticastListener());
        for (int i = 0; i < threadCount; i++) {
            receiveTasks.add(ServerManager.me().register(new UDPReceiveTask()));
        }
        ServerManager.me().run(this, "refreshSlicer");
    }


    @TaskFunction(name = "refreshSlicer")
    public void refreshSlicer() {
        try {
            slicer.refresh(refreshTimeout);
        } finally {
            ServerManager.me().run(this, (long) refreshTimeout, "refreshSlicer");
        }

    }

    @TaskProperties(name = "UDPMulticastReceiver::Receive", unique = false, preemptive = true)
    public class UDPReceiveTask extends AbstractTask {

        public UDPReceiveTask(String... name) {
            super(name);
            ServerManager.me().run(this, "receive");
        }

        /***
         * MESSAGE:
         * | TOPIC (String with end 0x00) | PAYLOAD (n bytes) |
         */
        @TaskFunction(name = "receive")
        public void receive() {
            try {
                receiver.invoke();
            } finally {
                ServerManager.me().run(this, "receive");
            }
        }

        @Override
        public void onClose() {
            try {
                sockets.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public class ReceiverMulticastListener extends AbstractDependent implements MulticastListener {

        private HashSet<Byte> interest = new HashSet<Byte>() {{
            add(UdpCmd.TOPIC.id);
        }};

        public ReceiverMulticastListener() {
            super(ReceiverMulticastListener.class);
        }

        @Override
        public Collection<Byte> getInterest() {
            return interest;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void process(String sender, UdpCmd cmd, byte[] payload) {
            ByteBuilder bb = new ByteBuilder(payload).resetPosition();
            String topic = bb.readStringWithTag();
            MessageHandle handle = ServerManager.me().getMessageHandle(topic);
            if (handle != null) {
                AutoBufAdapter adapter = adapterPool.getOrDefault(topic, null);
                if (adapter == null) {
                    adapter = new AutoBufAdapter(handle.getDataType());
                    adapterPool.put(topic, adapter);
                }

                Object msg;
                if (sendHeader) {
                    msg = adapter.decode(bb);
                } else {
                    msg = adapter.decodeBody(bb);
                }
                // TODO 填写udp ip 需要框架支持
                handle.pushSubData(msg, new MessageSource(sender, topic, MessageSourceType.UDP_MULTICAST, null, 0));
            }
        }
    }
}
