package cn.autolabor.message.sensor;

import cn.autolabor.message.common.MsgHeader;
import cn.autolabor.util.autobuf.SerializableMessage;

import java.util.Arrays;

public class MsgJoystick implements SerializableMessage {

    private MsgHeader header;
    /***
     *  | 方向键 (上下) (左右) | 左操作杆 (上下) (左右) | 右操作杆 (上下) (左右) |
     *  | X | A | B | Y | LB | RB | LT | RT | BACK | START |
     */
    private float[] axes;
    private byte[] buttons;

    public MsgJoystick() {
        this.header = new MsgHeader();
        this.axes = new float[6];
        this.buttons = new byte[10];
    }

    public MsgJoystick(float[] axes, byte[] buttons) {
        this.header = new MsgHeader();
        this.axes = axes;
        this.buttons = buttons;
    }

    public MsgHeader getHeader() {
        return header;
    }

    public void setHeader(MsgHeader header) {
        this.header = header;
    }

    public float[] getAxes() {
        return axes;
    }

    public void setAxes(float[] axes) {
        this.axes = axes;
    }

    public byte[] getButtons() {
        return buttons;
    }

    public void setButtons(byte[] buttons) {
        this.buttons = buttons;
    }

    @Override
    public String toString() {
        return "MsgJoystick{" + "header=" + header + ", axes=" + Arrays.toString(axes) + ", buttons=" + Arrays.toString(buttons) + '}';
    }
}
