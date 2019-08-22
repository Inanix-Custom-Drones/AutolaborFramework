package cn.autolabor.module.networkhub.remote.utilities;

import java.io.OutputStream;
import java.util.Arrays;

// 简易输出流
// 用于将字节读入固定字节数组
// 不可回退，不可重绑定
public final class SimpleOutputStream extends OutputStream {
    public final byte[] core;
    public int ptr = 0;

    public SimpleOutputStream(int size) {
        core = new byte[size];
    }

    public int available() {
        return core.length - ptr;
    }

    public void write(byte b) {
        core[ptr++] = b;
    }

    @Override
    public void write(int b) {
        core[ptr++] = (byte) b;
    }

    @Override
    public void write(byte[] b) {
        System.arraycopy(b, 0, core, ptr, b.length);
        ptr += b.length;
    }

    public void writeLength(byte[] b, int begin, int length) {
        System.arraycopy(b, begin, core, ptr, length);
        ptr += length;
    }

    public void writeFrom(SimpleInputStream stream, int length) {
        stream.readInto(this, length);
    }

    @Override
    public void close() {
        ptr = core.length;
    }

    // special

    // 写入一个带结尾'\0'的字符串
    public void writeEnd(String string) {
        write(string.getBytes());
        write(0);
    }

    public SimpleOutputStream zigzag(long num, boolean signed) {
        if (signed)
            num = (num << 1) ^ (num >> 63);
        while (true)
            if (num > 0x7f) {
                write((byte) (num | 0x80));
                num >>>= 7;
            } else {
                write((byte) num);
                return this;
            }
    }

    public byte[] toArray() {
        return Arrays.copyOf(core, ptr);
    }
}
