package cn.autolabor.message.app.location;

import cn.autolabor.util.autobuf.SerializableMessage;

public class NodeState implements SerializableMessage {
    // 信标ID
    private int id;
    // 版本号
    private int version;
    // 设备类型
    //10: wheel robot
    //12: crawler robot
    //16: beacon (4 sensors, HW V4.3)
    //17: hedgehog (4 sensors, HW V4.3)
    //18: modem (HW V4.3)
    //22: beacon (5 sensors, HW V4.5)
    //23: hedgehog (5 sensors, HW V4.5)
    //24: modem (HW V4.5/4.9)
    //30: beacon (5 sensors, HW V4.9)
    //31: hedgehog (5 sensors, HW V4.9)
    private int type;
    // 设备状态（0: 在线 -2：睡眠）
    private int state;
    // 电压mv
    private int voltage;
    // 电量状态(0:正常 -1:低 -2:超低)
    private int powerState;
    // RSSI
    private int rssi;
    // 温度°С
    private int temperature;
    // 该id是否有重复设备
    private boolean repeat;

    public NodeState() {
    }

    public NodeState(int id) {
        this.id = id;
    }

    public NodeState(int id, int version, int type, int state, int voltage,
            int powerState, int rssi, int temperature, boolean repeat) {
        this.id = id;
        this.version = version;
        this.type = type;
        this.state = state;
        this.voltage = voltage;
        this.powerState = powerState;
        this.rssi = rssi;
        this.temperature = temperature;
        this.repeat = repeat;
    }

    public int getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getVoltage() {
        return voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public int getPowerPercent() {
        int percent = 100 * (voltage - 3600) / (4200 - 3600);
        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }
        return percent;
    }

    public int getPowerState() {
        return powerState;
    }

    public void setPowerState(int powerState) {
        this.powerState = powerState;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    @Override
    public String toString() {
        return "NodeState{" +
                "id=" + id +
                ", version=" + (version >> 8) + "." + (version & 0xFF) +
                ", type=" + type +
                ", state=" + state +
                ", voltage=" + voltage + "(" + getPowerPercent() + "%)" +
                ", powerState=" + powerState +
                ", rssi=" + rssi +
                ", temperature=" + temperature +
                ", repeat=" + repeat +
                '}';
    }
}
