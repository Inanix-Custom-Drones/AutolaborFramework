package cn.autolabor.message.navigation;

import cn.autolabor.message.common.MsgHeader;
import cn.autolabor.util.autobuf.SerializableMessage;

import java.util.List;

public class MsgPolygon implements SerializableMessage {

    private MsgHeader header;
    private List<Msg2DPoint> points;

    public MsgPolygon() {
        this.header = new MsgHeader();
    }

    public MsgPolygon(String coordinate, List<Msg2DPoint> points) {
        this.header = new MsgHeader(coordinate);
        this.points = points;
    }

    public MsgPolygon(MsgHeader header, List<Msg2DPoint> points) {
        this.header = header;
        this.points = points;
    }

    public MsgHeader getHeader() {
        return header;
    }

    public void setHeader(MsgHeader header) {
        this.header = header;
    }

    public List<Msg2DPoint> getPoints() {
        return points;
    }

    public void setPoints(List<Msg2DPoint> points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "MsgPolygon{" + "header=" + header + ", points=" + points + '}';
    }
}
