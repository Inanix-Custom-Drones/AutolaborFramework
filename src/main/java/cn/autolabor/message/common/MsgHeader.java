package cn.autolabor.message.common;

import cn.autolabor.util.autobuf.SerializableMessage;
import cn.autolabor.util.autobuf.annotation.IgnoreField;

import java.util.concurrent.atomic.AtomicInteger;

public class MsgHeader implements SerializableMessage {

    @IgnoreField
    private static AtomicInteger sequenceGenerator = new AtomicInteger(0);

    private int seq;
    private long stamp;
    private String coordinate;

    public MsgHeader() {
        this(sequenceGenerator.getAndAdd(1), System.currentTimeMillis(), "");
    }

    public MsgHeader(long stamp) {
        this(sequenceGenerator.getAndAdd(1), stamp, "");
    }

    public MsgHeader(int seq, long time) {
        this(seq, time, "");
    }

    public MsgHeader(String coordinate) {
        this(sequenceGenerator.getAndAdd(1), System.currentTimeMillis(), coordinate);
    }

    public MsgHeader(int seq, long stamp, String coordinate) {
        this.seq = seq;
        this.stamp = stamp;
        this.coordinate = coordinate;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public long getStamp() {
        return stamp;
    }

    public void setStamp(long stamp) {
        this.stamp = stamp;
    }

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public String toString() {
        return "MsgHeader{" +
                "seq=" + seq +
                ", stamp=" + stamp +
                ", coordinate='" + coordinate + '\'' +
                '}';
    }
}
