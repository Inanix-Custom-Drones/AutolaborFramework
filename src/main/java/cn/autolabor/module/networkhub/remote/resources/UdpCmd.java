package cn.autolabor.module.networkhub.remote.resources;

import java.util.HashMap;

public enum UdpCmd implements Command {
    YELL_ASK(0),     // 存在性请求
    YELL_ACK(1),     // 存在性回复
    ADDRESS_ASK(2),  // 地址请求
    ADDRESS_ACK(3),  // 地址回复
    PACKET_SLICE(4), // 包分片
    TOPIC(5),
    COMMON(127);     // 通用广播

    public static final HashMap<Byte, UdpCmd> memory = buildMemory();

    public final byte id;

    UdpCmd(int id) {
        this.id = (byte) id;
    }

    private static HashMap<Byte, UdpCmd> buildMemory() {
        HashMap<Byte, UdpCmd> result = new HashMap<>();
        for (UdpCmd cmd : UdpCmd.values())
            result.put(cmd.id, cmd);
        return result;
    }

    @Override
    public byte getId() {
        return id;
    }
}
