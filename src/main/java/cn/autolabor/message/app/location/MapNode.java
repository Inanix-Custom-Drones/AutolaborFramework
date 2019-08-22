package cn.autolabor.message.app.location;

import cn.autolabor.message.app.graph.Point;
import cn.autolabor.util.autobuf.SerializableMessage;

public class MapNode implements SerializableMessage {
    // 信标ID
    private int id;
    // 信标状态（0: 在线 -1: 不在线 -2：睡眠）
    private int state;
    // 电量状态(0:正常 -1:低 -2:超低)
    private int powerState;
    // 电量百分比
    private int powerPercent;
    // 是否锁定
    private boolean freeze;
    // 是否参与定位
    private boolean inuse;
    // 坐标
    private Point point;

    public MapNode() {
    }

    public MapNode(int id) {
        this.id = id;
    }

    public MapNode(int id, int state, int powerState, int powerPercent,
            boolean freeze, boolean inuse, Point point) {
        this.id = id;
        this.state = state;
        this.freeze = freeze;
        this.powerState = powerState;
        this.powerPercent = powerPercent;
        this.inuse = inuse;
        this.point = point;
    }

    public int getId() {
        return id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getPowerState() {
        return powerState;
    }

    public void setPowerState(int powerState) {
        this.powerState = powerState;
    }

    public int getPowerPercent() {
        return powerPercent;
    }

    public void setPowerPercent(int powerPercent) {
        this.powerPercent = powerPercent;
    }

    public boolean getFreeze() {
        return freeze;
    }

    public void setFreeze(boolean freeze) {
        this.freeze = freeze;
    }

    public boolean isInuse() {
        return inuse;
    }

    public void setInuse(boolean inuse) {
        this.inuse = inuse;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point.setX(point.getX());
        this.point.setY(point.getY());
        this.point.setZ(point.getZ());
    }

    @Override
    public String toString() {
        return "MapNode{" +
                "ID=" + id +
                ", 状态=" + (state == 0 ? "在线" : (state == -1 ? "离线" : "睡眠")) +
                ", 电量=" + (powerState == 0 ? "正常" : (powerState == -1 ? "低" : "超低")) +
                ", 电量(%)=" + powerPercent + "%" +
                ", " + (freeze ? "锁定" : "未锁定") +
                ", " + (inuse ? "参与定位" : "未参与定位") +
                ", 坐标=" + point +
                '}';
    }
}
