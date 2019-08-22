package cn.autolabor.module.communication;

import cn.autolabor.util.autobuf.ByteBuilder;

import java.io.IOException;
import java.io.InputStream;

public class TCPUtil {

    public static ByteBuilder getByteFromInputStream(InputStream is) {
        try {
            byte[] lenBytes = new byte[4];
            is.read(lenBytes, 0, 4);
            int len = (((lenBytes[3] & 0xff) << 24) | ((lenBytes[2] & 0xff) << 16) |
                    ((lenBytes[1] & 0xff) << 8) | (lenBytes[0] & 0xff));
            byte[] receivedBytes = new byte[len];
            is.read(receivedBytes, 0, len);
            return new ByteBuilder(receivedBytes).resetPosition();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
