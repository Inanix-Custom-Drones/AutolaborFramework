package cn.autolabor.module.communication;

import cn.autolabor.core.annotation.*;
import cn.autolabor.core.server.ServerManager;
import cn.autolabor.core.server.executor.AbstractTask;
import cn.autolabor.core.server.executor.CallbackItem;
import cn.autolabor.core.server.message.MessageHandle;
import cn.autolabor.message.system.MsgTCPServerPing;
import cn.autolabor.message.system.MsgTCPTrigger;
import cn.autolabor.util.autobuf.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@TaskProperties(name = "TCPClientSupport")
public class TCPClientSupport extends AbstractTask {

    public static Map<String, TCPDeviceBean> deviceMap = new ConcurrentHashMap<>();
    @TaskParameter(name = "connectTimeout", value = "500")
    private static int connectTimeout;
    private static boolean support = false;
    private static Map<String, Object> waitObject = new ConcurrentHashMap<>();

    static {
        ServerManager.me().register(new TCPClientSupport());
    }

    @InjectMessage(topic = "tcp_echo")
    private MessageHandle<MsgTCPTrigger> triggerMessageHandle;
    @TaskParameter(name = "deviceTimeout", value = "5000")
    private int deviceTimeout;

    private TCPClientSupport(String... name) {
        super(name);
        ServerManager.me().run(this, "checkTimeout");
        ServerManager.me().run(this, "startTrigger");
        support = true;
    }

    /***
     *  request :  | taskName (end with 0x00) | eventName (end with 0x00) | params |
     *  response : | status (bit8) | result |
     */
    public static TCPResponse callOne(String deviceId, TCPRequest request) {
        long start = System.currentTimeMillis();
        while ((!deviceMap.containsKey(deviceId)) && System.currentTimeMillis() - start < connectTimeout) {
            Object waitItem;
            if (!waitObject.containsKey(deviceId)) {
                waitItem = new Object();
                waitObject.put(deviceId, waitItem);
            } else {
                waitItem = waitObject.get(deviceId);
            }

            synchronized (waitItem) {
                try {
                    waitItem.wait(start + connectTimeout - System.currentTimeMillis());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        TCPDeviceBean bean = deviceMap.getOrDefault(deviceId, null);
        if (bean != null) {
            return call(bean.getIp(), bean.getPort(), request);
        } else {
            return new TCPResponse(TCPRespStatusType.NO_DEVICE, null);
        }
    }

    private static TCPResponse call(String ip, int port, TCPRequest request) {
        OutputStream os = null;
        InputStream is = null;
        Socket socket = null;
        try {
            ByteBuilder bb = new ByteBuilder();
            bb.putStringWithTag(request.getTaskName());
            bb.putStringWithTag(request.getEventName());
            if (request.hasParam()) {
                Map<String, Object> params = request.getParams();
                AutoBufEmbedded embedded = AutoBufBuilder.createEmbedded(null, "Params");
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    embedded.putRawData(entry.getKey(), entry.getValue());
                }
                bb.putBytes(AutoBufEncoder.toBytes(embedded));
            }

            socket = new Socket(ip, port);
            os = socket.getOutputStream();
            os.write(new ByteBuilder().putInt(bb.getLimit()).putBytes(bb.toBytes()).toBytes());
            os.flush();

            is = socket.getInputStream();
            ByteBuilder returnBB = TCPUtil.getByteFromInputStream(is);
            if (returnBB != null) {
                TCPResponse response = new TCPResponse();
                response.setStatus(TCPRespStatusType.getTypeFromCode(returnBB.readByte()));
                if (returnBB.getPosition() < returnBB.getLimit()) {
                    response.setResult(AutoBufDecoder.toObject(returnBB));
                }
                return response;
            } else {
                return new TCPResponse(TCPRespStatusType.FAILURE, null);
            }
        } catch (IOException e) {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return new TCPResponse(TCPRespStatusType.NO_DEVICE, null);
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
    }

    @TaskFunction(name = "startTrigger")
    public void startTrigger() {
        boolean hasUDP = false;
        Set<CallbackItem> items = triggerMessageHandle.getCallbackFunctionItems();
        for (CallbackItem item : items) {
            if (item.getTask().getClass().equals(UDPMulticastSupport.class) && item.getEvent().equals("distributeMessage")) {
                hasUDP = true;
                break;
            }
        }
        if (hasUDP) {
            triggerMessageHandle.pushSubData(new MsgTCPTrigger());
        } else {
            ServerManager.me().run(this, "startTrigger");
        }
    }

    @SubscribeMessage(topic = "tcp_ping")
    @TaskFunction(name = "updateDevice")
    public void updateDevice(MsgTCPServerPing msg) {
        String id = msg.getDeviceId();
        if (!Objects.equals(id, ServerManager.getIdentification())) {
            if (deviceMap.containsKey(id)) {
                TCPDeviceBean deviceBean = deviceMap.get(id);
                if (!Objects.equals(deviceBean.getIp(), msg.getIp()) || deviceBean.getPort() != msg.getPort()) {
                    deviceBean.setIp(msg.getIp());
                    deviceBean.setPort(msg.getPort());
                }
                deviceBean.setLastTime(System.currentTimeMillis());
            } else {
                TCPDeviceBean deviceBean = new TCPDeviceBean(msg.getIp(), msg.getPort(), System.currentTimeMillis());
                deviceMap.put(id, deviceBean);
                if (waitObject.containsKey(id)) {
                    Object waitItem = waitObject.get(id);
                    synchronized (waitItem) {
                        waitItem.notifyAll();
                    }
                }
            }
        }
    }

    @TaskFunction(name = "checkTimeout")
    public void checkTimeout() {
        long currentTime = System.currentTimeMillis();
        Set<String> removeKey = new HashSet<>();
        for (Map.Entry<String, TCPDeviceBean> entry : deviceMap.entrySet()) {
            if (currentTime - entry.getValue().getLastTime() > deviceTimeout) {
                removeKey.add(entry.getKey());
            }
        }

        for (String key : removeKey) {
            deviceMap.remove(key);
        }

        ServerManager.me().run(this, (long) deviceTimeout, "checkTimeout");
    }
}
