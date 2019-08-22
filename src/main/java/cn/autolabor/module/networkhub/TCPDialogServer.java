package cn.autolabor.module.networkhub;

import cn.autolabor.core.annotation.FilterTask;
import cn.autolabor.core.annotation.TaskFunction;
import cn.autolabor.core.annotation.TaskParameter;
import cn.autolabor.core.annotation.TaskProperties;
import cn.autolabor.core.server.ServerManager;
import cn.autolabor.core.server.executor.AbstractTask;
import cn.autolabor.core.server.executor.TaskMethod;
import cn.autolabor.module.communication.TCPRespStatusType;
import cn.autolabor.module.networkhub.remote.modules.tcpconnection.DialogTcpServer;
import cn.autolabor.module.networkhub.remote.modules.tcpconnection.PortBroadcaster;
import cn.autolabor.module.networkhub.remote.modules.tcpconnection.ShortConnectionServer;
import cn.autolabor.module.networkhub.remote.resources.ServerSockets;
import cn.autolabor.util.autobuf.AutoBufAdapter;
import cn.autolabor.util.autobuf.AutoBufDecoder;
import cn.autolabor.util.autobuf.AutoBufEmbedded;
import cn.autolabor.util.autobuf.ByteBuilder;
import cn.autolabor.util.reflect.TypeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@TaskProperties(name = "TCPDialogServer")
public class TCPDialogServer extends AbstractTask {

    private final ShortConnectionServer server;
    @TaskParameter(name = "serverPort", value = "0")
    private int serverPort;
    @TaskParameter(name = "receiveThreadCount", value = "1")
    private int receiveThreadCount;
    private boolean udpMulticastBroadcasterExist = false;
    private boolean udpMulticastReceiverExist = false;
    private List<TCPReceiveHandleTask> receiveHandleTasks = new ArrayList<>();

    public TCPDialogServer(String... name) {
        super(name);
        RemoteHub.ME.setAndGet(new ServerSockets());
        server = RemoteHub.ME.setAndGet(new ShortConnectionServer());
        RemoteHub.ME.setAndGet(new DialogTcpServer(this::handleMsg));

        for (int i = 0; i < receiveThreadCount; i++) {
            receiveHandleTasks.add(ServerManager.me().register(new TCPReceiveHandleTask()));
        }
    }

    @FilterTask
    @TaskFunction(name = "detectUDP")
    public void detectUDP(AbstractTask task) {
        if (!udpMulticastBroadcasterExist || !udpMulticastReceiverExist) {
            if (task.getClass().equals(UDPMulticastBroadcaster.class)) {
                udpMulticastBroadcasterExist = true;
            }
            if (task.getClass().equals(UDPMulticastReceiver.class)) {
                udpMulticastReceiverExist = true;
            }

            if (udpMulticastReceiverExist && udpMulticastBroadcasterExist) {
                RemoteHub.ME.setAndGet(new PortBroadcaster());
                ServerManager.me().removeRegisterTaskCallback(this, "detectUDP");
            }
        }
    }

    private byte[] handleMsg(String client, byte[] request) {
        ByteBuilder bb = new ByteBuilder(request).resetPosition();
        String taskName = bb.readStringWithTag();
        String eventName = bb.readStringWithTag();

        AutoBufEmbedded params = null;
        if (bb.getPosition() != bb.getLimit()) {
            params = (AutoBufEmbedded) AutoBufDecoder.toObject(bb);
        }
        return response(taskName, eventName, params);
    }

    private byte[] response(String taskName, String eventName, AutoBufEmbedded params) {
        AbstractTask task = ServerManager.me().getTaskByName(taskName);
        if (task == null) {
            return buildResp(TCPRespStatusType.NO_TASK, null, null);
        }

        TaskMethod method = task.getEvent(eventName);
        if (method == null) {
            return buildResp(TCPRespStatusType.NO_METHOD, null, null);
        }

        final ReentrantLock lock = task.runLock;
        lock.lock();
        try {
            Object[] rawParam = null;
            int paramCount = method.getParamsType().length;
            if (paramCount > 0) {
                rawParam = new Object[paramCount];
                for (int i = 0; i < paramCount; i++) {
                    rawParam[i] = params.getRaw(String.format("arg%d", i), method.getParamsType()[i]);
                }
            }
            Object result = method.getFun().invoke(rawParam);
            return buildResp(TCPRespStatusType.SUCCESS, result, method.getReturnType());
        } catch (IllegalArgumentException e) {
            return buildResp(TCPRespStatusType.ILLEGAL_ARGUMENT, null, null);
        } finally {
            lock.unlock();
        }
    }

    private byte[] buildResp(TCPRespStatusType type, Object result, TypeNode typeNode) {
        ByteBuilder bb = new ByteBuilder();
        bb.putByte(type.getCode());
        if (result != null) {
            bb.putBytes(new AutoBufAdapter(typeNode).encode(result));
        }
        return bb.toBytes();
    }


    @TaskProperties(name = "TCPDialogServer::Handle", unique = false, preemptive = true)
    public class TCPReceiveHandleTask extends AbstractTask {

        public TCPReceiveHandleTask(String... name) {
            super(name);
            ServerManager.me().run(this, "receive");
        }

        @TaskFunction(name = "receive")
        public void receive() {
            try {
                server.invoke(serverPort);
            } finally {
                ServerManager.me().run(this, "receive");
            }
        }
    }
}
