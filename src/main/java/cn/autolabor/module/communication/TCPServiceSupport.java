package cn.autolabor.module.communication;

import cn.autolabor.core.annotation.*;
import cn.autolabor.core.server.ServerManager;
import cn.autolabor.core.server.executor.AbstractTask;
import cn.autolabor.core.server.executor.TaskMethod;
import cn.autolabor.core.server.message.MessageHandle;
import cn.autolabor.core.server.message.MessageSource;
import cn.autolabor.message.system.MsgTCPServerPing;
import cn.autolabor.message.system.MsgTCPTrigger;
import cn.autolabor.util.Sugar;
import cn.autolabor.util.autobuf.AutoBufAdapter;
import cn.autolabor.util.autobuf.AutoBufDecoder;
import cn.autolabor.util.autobuf.AutoBufEmbedded;
import cn.autolabor.util.autobuf.ByteBuilder;
import cn.autolabor.util.reflect.TypeNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

@TaskProperties(name = "TCPServiceSupport")
public class TCPServiceSupport extends AbstractTask {

    public static boolean support = false;
    protected static Logger log = Logger.getLogger(TCPServiceSupport.class.getName());
    @TaskParameter(name = "networkInterfaceName", value = "")
    private String networkInterfaceName;
    @TaskParameter(name = "serverPort", value = "0")
    private int serverPort;
    @TaskParameter(name = "receiveThreadCount", value = "1")
    private int receiveThreadCount;
    @TaskParameter(name = "backlog", value = "20")
    private int backlog;
    private List<TCPReceiveHandleTask> receiveHandleTasks = new ArrayList<>();
    private ServerSocket serverSocket;
    private Random rand = new Random();


    public TCPServiceSupport(String... name) {
        super(name);
        try {
            NetworkInterface networkInterface = Sugar.tryGetNetworkInterface(networkInterfaceName);
            InetAddress inetAddress = Sugar.getIpv4Address(networkInterface);
            if (inetAddress != null) {
                serverSocket = new ServerSocket(serverPort, backlog, inetAddress);
                log.info("Open The TCP Server(" + ServerManager.getIdentification() + ") => " + serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
                for (int i = 0; i < receiveThreadCount; i++) {
                    receiveHandleTasks.add(ServerManager.me().register(new TCPReceiveHandleTask()));
                }
                ServerManager.me().register(new TcpReceiveTask());
                ServerManager.me().register(new Ping());
                support = true;
            } else {
                log.info("Unable to open TCP Server due to can not find a suitable InetAddress.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TCPServiceSupport tcp = new TCPServiceSupport();
    }

    private void handleSocket(Socket socket) {
        for (TCPReceiveHandleTask handleTask : receiveHandleTasks) {
            if (!handleTask.isProcessing()) {
                ServerManager.me().run(handleTask, "handle", socket);
                return;
            }
        }
        ServerManager.me().run(receiveHandleTasks.get(rand.nextInt(receiveHandleTasks.size())), "handle", socket);
    }

    public boolean isSupport() {
        return support;
    }

    @TaskProperties(name = "TCPServiceSupport::Ping")
    public class Ping extends AbstractTask {

        @InjectMessage(topic = "tcp_ping")
        MessageHandle<MsgTCPServerPing> pingHandle;
        @TaskParameter(name = "interval", value = "2000")
        private int interval;

        public Ping(String... name) {
            super(name);
            ServerManager.me().run(this, "loop");
        }

        @TaskFunction(name = "loop")
        public void loop() {
            MsgTCPServerPing msgPing = new MsgTCPServerPing(ServerManager.getIdentification(), serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
            pingHandle.pushSubData(msgPing);
            ServerManager.me().run(this, (long) interval, "loop");
        }

        @SubscribeMessage(topic = "tcp_echo")
        @TaskFunction(name = "echo")
        public void echo(MsgTCPTrigger trigger, MessageSource source) {
            MsgTCPServerPing msgPing = new MsgTCPServerPing(ServerManager.getIdentification(), serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
            pingHandle.pushSubData(msgPing);
        }
    }

    @TaskProperties(name = "TCPServiceSupport::Receive", preemptive = true)
    public class TcpReceiveTask extends AbstractTask {
        public TcpReceiveTask(String... name) {
            super(name);
            ServerManager.me().run(this, "receive");
        }

        @TaskFunction(name = "receive")
        public void receive() {
            try {
                Socket socket = serverSocket.accept();
                handleSocket(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ServerManager.me().run(this, "receive");
        }
    }

    /***
     *  request :  | taskName (end with 0x00) | eventName (end with 0x00) | params |
     *  response : |
     *  | result |
     */
    @TaskProperties(name = "TCPServiceSupport::Handle", unique = false)
    public class TCPReceiveHandleTask extends AbstractTask {
        private boolean isProcessing;

        public TCPReceiveHandleTask(String... name) {
            super(name);
            isProcessing = false;
        }

        @TaskFunction(name = "handle")
        public void handle(Socket socket) {
            isProcessing = true;
            InputStream is = null;
            OutputStream os = null;
            try {
                is = socket.getInputStream();
                ByteBuilder bb = TCPUtil.getByteFromInputStream(is);
                if (bb != null) {
                    // | taskName (end with 0x00) | eventName (end with 0x00) | params |
                    String taskName = bb.readStringWithTag();
                    String eventName = bb.readStringWithTag();
                    AutoBufEmbedded params = null;
                    if (bb.getPosition() != bb.getLimit()) {
                        params = (AutoBufEmbedded) AutoBufDecoder.toObject(bb);
                    }
                    // | status (bit8) | result |
                    os = socket.getOutputStream();
                    os.write(response(taskName, eventName, params));
                } else {
                    os = socket.getOutputStream();
                    os.write(buildResp(TCPRespStatusType.FAILURE, null, null));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }

                    if (os != null) {
                        os.close();
                    }

                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            isProcessing = false;
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
            return new ByteBuilder().putInt(bb.getLimit()).putBytes(bb).toBytes();
        }

        public boolean isProcessing() {
            return isProcessing;
        }
    }
}
