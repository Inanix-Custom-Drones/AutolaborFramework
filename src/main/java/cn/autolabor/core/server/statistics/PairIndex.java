package cn.autolabor.core.server.statistics;

import cn.autolabor.util.autobuf.SerializableMessage;

public class PairIndex implements SerializableMessage {

    private int x;
    private int y;

    public PairIndex(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PairIndex pairIndex = (PairIndex) o;

        if (x != pairIndex.x)
            return false;
        return y == pairIndex.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "PairIndex{" + "x=" + x + ", y=" + y + '}';
    }
}
