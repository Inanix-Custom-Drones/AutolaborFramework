package cn.autolabor.message.system;

import cn.autolabor.util.autobuf.SerializableMessage;

public class MsgTCPServerPing implements SerializableMessage {

    private String deviceId;
    private String ip;
    private int port;

    public MsgTCPServerPing() {
    }

    public MsgTCPServerPing(String deviceId, String ip, int port) {
        this.deviceId = deviceId;
        this.ip = ip;
        this.port = port;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "MsgTCPServerPing{" +
                "deviceId='" + deviceId + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
