package cn.autolabor.message.sensor;

import cn.autolabor.message.common.MsgHeader;
import cn.autolabor.util.autobuf.SerializableMessage;

import java.util.List;

public class MsgLidar implements SerializableMessage {

    private MsgHeader header;
    private List<Double> angles;
    private List<Double> distances;

    public MsgLidar() {
        this.header = new MsgHeader();
    }

    public MsgLidar(String coordinate, List<Double> angles, List<Double> distances) {
        this.header = new MsgHeader(coordinate);
        this.angles = angles;
        this.distances = distances;
    }

    public MsgHeader getHeader() {
        return header;
    }

    public void setHeader(MsgHeader header) {
        this.header = header;
    }

    public List<Double> getAngles() {
        return angles;
    }

    public void setAngles(List<Double> angles) {
        this.angles = angles;
    }

    public List<Double> getDistances() {
        return distances;
    }

    public void setDistances(List<Double> distances) {
        this.distances = distances;
    }

    @Override
    public String toString() {
        return "MsgLidar{" + "header=" + header + ", angles=" + angles + ", distances=" + distances + '}';
    }
}
