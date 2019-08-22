package cn.autolabor.module.networkhub.remote.utilities;

import java.io.InputStream;

// 简易输入流
// 用于从固定字节数组的指定范围读出字节
// 不可回退，不可重绑定
public final class SimpleInputStream extends InputStream {
    private final byte[] core; // 内核数组的引用，只读
    private final int end;     // 流结尾
    private int ptr;           // 流开头

    public SimpleInputStream(byte[] core, int ptr, int end) {
        this.core = core;
        this.ptr = ptr;
        this.end = end;
    }

    public SimpleInputStream(byte[] core) {
        this.core = core;
        this.ptr = 0;
        this.end = core.length;
    }

    @Override
    public int available() {
        return end - ptr;
    }

    @Override
    public int read() {
        return ptr < end
                ? core[ptr] >= 0 ? core[ptr++] : core[ptr++] + 256
                : -1;
    }

    // 查看当前流顶端的字节
    public byte look() {
        return core[ptr];
    }

    // 跳过指定字节数并返回自身
    public SimpleInputStream skip(int n) {
        ptr += n;
        return this;
    }

    // 查看剩余的所有字节
    public byte[] lookRest() {
        byte[] result = new byte[end - ptr];
        System.arraycopy(core, ptr, result, 0, result.length);
        return result;
    }

    void readInto(SimpleOutputStream stream, int length) {
        stream.writeLength(core, ptr, length);
        ptr += length;
    }

    @Override
    public void close() {
        ptr = end;
    }

    // special

    // 读出一个带结尾'\0'的字符串
    // 无安全保障，请先确定流里确有结尾'\0'，否则将引发异常
    public String readEnd() {
        int zero = ptr;
        //noinspection StatementWithEmptyBody
        while (core[zero++] != 0)
            ;
        byte[] result = new byte[zero - ptr - 1];
        System.arraycopy(core, ptr, result, 0, result.length);
        ptr = zero;
        return new String(result);
    }

    // 读出一个变长编码整数
    public Long zigzag(boolean signed) {
        byte offset = 0;
        long result = 0;

        int b;
        do {
            b = read();
            result |= (long) (b & 0x7f) << 7 * offset++;
        } while (b > 0x7f);

        return signed
                ? result >>> 1 ^ -(result & 1)
                : result;
    }
}
