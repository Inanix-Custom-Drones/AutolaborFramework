package cn.autolabor.message.navigation;

import cn.autolabor.util.autobuf.SerializableMessage;

public class Msg2DPoint implements SerializableMessage {
    private double x;
    private double y;

    public Msg2DPoint() {
    }

    public Msg2DPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Msg2DPoint{" + "x=" + x + ", y=" + y + '}';
    }
}
