package cn.autolabor.module.communication;

public class TCPDeviceBean {

    private String ip;
    private int port;
    private long lastTime;

    public TCPDeviceBean(String ip, int port, long lastTime) {
        this.ip = ip;
        this.port = port;
        this.lastTime = lastTime;
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

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    @Override
    public String toString() {
        return "TCPDeviceBean{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", lastTime=" + lastTime +
                '}';
    }
}
