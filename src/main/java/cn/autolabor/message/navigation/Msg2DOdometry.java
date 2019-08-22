package cn.autolabor.message.navigation;

import cn.autolabor.message.common.MsgHeader;
import cn.autolabor.util.autobuf.SerializableMessage;

public class Msg2DOdometry implements SerializableMessage {

    private MsgHeader header;
    private Msg2DPose pose;
    private Msg2DTwist twist;

    public Msg2DOdometry() {
        header = new MsgHeader();
    }

    public Msg2DOdometry(Msg2DPose pose, Msg2DTwist twist) {
        this.header = new MsgHeader();
        this.pose = pose;
        this.twist = twist;
    }

    public MsgHeader getHeader() {
        return header;
    }

    public void setHeader(MsgHeader header) {
        this.header = header;
    }

    public Msg2DPose getPose() {
        return pose;
    }

    public void setPose(Msg2DPose pose) {
        this.pose = pose;
    }

    public Msg2DTwist getTwist() {
        return twist;
    }

    public void setTwist(Msg2DTwist twist) {
        this.twist = twist;
    }

    @Override
    public String toString() {
        return "Msg2DOdometry{" + "header=" + header + ", pose=" + pose + ", twist=" + twist + '}';
    }
}
