package cn.autolabor.message.sensor;

import cn.autolabor.message.common.MsgHeader;
import cn.autolabor.message.common.Quaternion;
import cn.autolabor.message.common.Vector3;
import cn.autolabor.util.autobuf.SerializableMessage;

public class MsgImu implements SerializableMessage {

    private MsgHeader header;
    private Quaternion orientation;
    private Vector3 angularVelocity;
    private Vector3 linearAcceleration;

    public MsgImu() {
        this.header = new MsgHeader();
    }

    public MsgImu(String coordinate) {
        this.header = new MsgHeader(coordinate);
    }

    public MsgImu(MsgHeader header, Quaternion orientation, Vector3 angularVelocity, Vector3 linearAcceleration) {
        this.header = header;
        this.orientation = orientation;
        this.angularVelocity = angularVelocity;
        this.linearAcceleration = linearAcceleration;
    }

    public MsgImu(Quaternion orientation, Vector3 angularVelocity, Vector3 linearAcceleration) {
        this.header = new MsgHeader();
        this.orientation = orientation;
        this.angularVelocity = angularVelocity;
        this.linearAcceleration = linearAcceleration;
    }

    public MsgHeader getHeader() {
        return header;
    }

    public void setHeader(MsgHeader header) {
        this.header = header;
    }

    public Quaternion getOrientation() {
        return orientation;
    }

    public void setOrientation(Quaternion orientation) {
        this.orientation = orientation;
    }

    public Vector3 getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(Vector3 angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public Vector3 getLinearAcceleration() {
        return linearAcceleration;
    }

    public void setLinearAcceleration(Vector3 linearAcceleration) {
        this.linearAcceleration = linearAcceleration;
    }

    @Override
    public String toString() {
        return "MsgImu{" +
                "header=" + header +
                ", orientation=" + orientation +
                ", angularVelocity=" + angularVelocity +
                ", linearAcceleration=" + linearAcceleration +
                '}';
    }
}
