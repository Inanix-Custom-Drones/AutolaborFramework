package cn.autolabor.module.networkhub.remote.resources;

import java.util.HashMap;

public enum TcpCmd implements Command {
    Dialog(1),
    COMMON(127);

    public static final HashMap<Byte, TcpCmd> memory = buildMemory();

    public final byte id;

    TcpCmd(int id) {
        this.id = (byte) id;
    }

    private static HashMap<Byte, TcpCmd> buildMemory() {
        HashMap<Byte, TcpCmd> result = new HashMap<>();
        for (TcpCmd cmd : TcpCmd.values())
            result.put(cmd.id, cmd);
        return result;
    }

    @Override
    public byte getId() {
        return id;
    }
}
