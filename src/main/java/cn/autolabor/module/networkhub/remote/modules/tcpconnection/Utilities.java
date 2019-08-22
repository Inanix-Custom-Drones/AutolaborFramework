package cn.autolabor.module.networkhub.remote.modules.tcpconnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Utilities {
    static void say(Socket socket, byte cmd) {
        try {
            socket.getOutputStream().write(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void say(Socket socket, byte[] payload) {
        try {
            int num = payload.length;
            OutputStream stream = socket.getOutputStream();
            while (true)
                if (num > 0x7f) {
                    stream.write((byte) (num | 0x80));
                    num >>>= 7;
                } else {
                    stream.write((byte) num);
                    break;
                }
            stream.write(payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static byte ListenCommand(Socket socket) {
        try {
            return (byte) socket.getInputStream().read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (byte) 255;
    }

    public static byte[] listen(Socket socket) {

        byte offset = 0;
        int length = 0;
        try {
            InputStream stream = socket.getInputStream();
            int b;
            do {
                b = stream.read();
                length |= (b & 0x7f) << 7 * offset++;
            } while (b > 0x7f);

            while (stream.available() < length)
                Thread.yield();

            byte[] result = new byte[length];
            if (length != stream.read(result))
                throw new RuntimeException("it's not a complete sentence!");

            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
