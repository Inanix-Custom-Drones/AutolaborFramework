package cn.autolabor.util.autobuf;

import cn.autolabor.util.Strings;
import cn.autolabor.util.Sugar;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public final class ByteBuilder {

    private final static int DEFAULT_CAPACITY = 10;
    private final static int BYTE_SIZE = 1;
    private final static int SHORT_SIZE = 2;
    private final static int INT_SIZE = 4;
    private final static int LONG_SIZE = 8;

    private byte[] data;
    private int capacity;
    private int limit;
    private int position;

    public ByteBuilder() {
        this(DEFAULT_CAPACITY);
    }

    public ByteBuilder(int capacity) {
        data = new byte[capacity];
        this.capacity = capacity;
        this.limit = 0;
        this.position = 0;
    }

    public ByteBuilder(byte[] array) {
        this.data = Arrays.copyOf(array, array.length);
        this.capacity = array.length;
        this.limit = array.length;
        this.position = array.length;
    }

    public static void main(String[] args) {
        ByteBuilder bb = new ByteBuilder();
        bb.putUint(263);
        bb.resetPosition();
        int i = bb.readUint();
        System.out.println(i);

    }

    public ByteBuilder clear() {
        this.position = 0;
        this.limit = 0;
        return this;
    }

    public ByteBuilder clearTo(int index) {
        this.position = index;
        this.limit = index;
        return this;
    }

    public ByteBuilder resetPosition() {
        this.position = 0;
        return this;
    }

    public ByteBuilder putBoolean(boolean b) {
        if (b) {
            return putByte((byte) 0x01);
        } else {
            return putByte((byte) 0x00);
        }
    }

    public boolean readBoolean() {
        byte b = readByte();
        return b == 0x01;
    }

    public ByteBuilder putByte(byte b) {
        ensureBounds(BYTE_SIZE);
        data[position++] = b;
        setLimit();
        return this;
    }

    public byte readByte() {
        checkBounds(BYTE_SIZE);
        return data[position++];
    }

    public ByteBuilder putBytes(byte[] bs) {
        ensureBounds(bs.length);
        System.arraycopy(bs, 0, data, position, bs.length);
        position += bs.length;
        setLimit();
        return this;
    }

    public ByteBuilder putBytes(byte[] bs, int off, int len) {
        ensureBounds(len);
        System.arraycopy(bs, off, data, position, len);
        position += len;
        setLimit();
        return this;
    }

    public ByteBuilder putBytes(ByteBuilder bb) {
        ensureBounds(bb.limit);
        System.arraycopy(bb.data, 0, data, position, bb.limit);
        position += bb.limit;
        setLimit();
        return this;
    }

    public byte[] readBytes(int len) {
        checkBounds(len);
        byte[] result = Arrays.copyOfRange(data, position, position + len);
        position += len;
        return result;
    }

    public ByteBuilder putChar(char b) {
        ensureBounds(SHORT_SIZE);
        data[position++] = (byte) (b);
        data[position++] = (byte) (b >> 8);
        setLimit();
        return this;
    }

    public char readChar() {
        checkBounds(SHORT_SIZE);
        return (char) ((data[position++] & 0xff) | (data[position++] & 0xff) << 8);
    }

    public ByteBuilder putShort(short b) {
        ensureBounds(SHORT_SIZE);
        data[position++] = (byte) (b);
        data[position++] = (byte) (b >> 8);
        setLimit();
        return this;
    }

    public short readShort() {
        checkBounds(SHORT_SIZE);
        return (short) ((data[position++] & 0xff) | (data[position++] & 0xff) << 8);
    }

    public ByteBuilder putInt(int b) {
        ensureBounds(INT_SIZE);
        data[position++] = (byte) (b);
        data[position++] = (byte) (b >>> 8);
        data[position++] = (byte) (b >>> 16);
        data[position++] = (byte) (b >>> 24);
        setLimit();
        return this;
    }

    public int readInt() {
        checkBounds(INT_SIZE);
        return (data[position++] & 0xff) | ((data[position++] & 0xff) << 8) | ((data[position++] & 0xff) << 16) | ((data[position++] & 0xff) << 24);
    }

    public ByteBuilder putUint(int b) {
        ensureBounds(INT_SIZE + 1);
        if ((b & ~0x7F) != 0) {
            data[position++] = (byte) ((b | 0x80) & 0xFF);
            b >>>= 7;
            if (b > 0x7F) {
                data[position++] = (byte) ((b | 0x80) & 0xFF);
                b >>>= 7;
                if (b > 0x7F) {
                    data[position++] = (byte) ((b | 0x80) & 0xFF);
                    b >>>= 7;
                    if (b > 0x7F) {
                        data[position++] = (byte) ((b | 0x80) & 0xFF);
                        b >>>= 7;
                    }
                }
            }
        }
        data[position++] = (byte) b;
        setLimit();
        return this;
    }

    public int readUint() {
        int b = readByte() & 0xff;
        int n = b & 0x7f;
        if (b > 0x7f) {
            b = readByte() & 0xff;
            n ^= (b & 0x7f) << 7;
            if (b > 0x7f) {
                b = readByte() & 0xff;
                n ^= (b & 0x7f) << 14;
                if (b > 0x7f) {
                    b = readByte() & 0xff;
                    n ^= (b & 0x7f) << 21;
                    if (b > 0x7f) {
                        b = readByte() & 0xff;
                        n ^= (b & 0x7f) << 28;
                        if (b > 0x7f) {
                            throw Sugar.makeThrow("Invalid int encoding");
                        }
                    }
                }
            }
        }
        return n;
    }

    public ByteBuilder putSint(int b) {
        b = (b << 1) ^ (b >> 31);
        return putUint(b);
    }

    public int readSint() {
        int n = readUint();
        return (n >>> 1) ^ -(n & 1);
    }

    public ByteBuilder putLong(long b) {
        ensureBounds(LONG_SIZE);
        data[position++] = (byte) (b);
        data[position++] = (byte) (b >>> 8);
        data[position++] = (byte) (b >>> 16);
        data[position++] = (byte) (b >>> 24);
        data[position++] = (byte) (b >>> 32);
        data[position++] = (byte) (b >>> 40);
        data[position++] = (byte) (b >>> 48);
        data[position++] = (byte) (b >>> 56);
        setLimit();
        return this;
    }

    public long readLong() {
        checkBounds(LONG_SIZE);
        int n1 = (data[position++] & 0xff) | ((data[position++] & 0xff) << 8) | ((data[position++] & 0xff) << 16) | ((data[position++] & 0xff) << 24);
        int n2 = (data[position++] & 0xff) | ((data[position++] & 0xff) << 8) | ((data[position++] & 0xff) << 16) | ((data[position++] & 0xff) << 24);
        return (((long) n1) & 0xffffffffL) | (((long) n2) << 32);
    }

    public ByteBuilder putUlong(long b) {
        ensureBounds(LONG_SIZE + 2);
        if ((b & ~0x7FL) != 0) {
            data[position++] = (byte) ((b | 0x80) & 0xFF);
            b >>>= 7;
            if (b > 0x7F) {
                data[position++] = (byte) ((b | 0x80) & 0xFF);
                b >>>= 7;
                if (b > 0x7F) {
                    data[position++] = (byte) ((b | 0x80) & 0xFF);
                    b >>>= 7;
                    if (b > 0x7F) {
                        data[position++] = (byte) ((b | 0x80) & 0xFF);
                        b >>>= 7;
                        if (b > 0x7F) {
                            data[position++] = (byte) ((b | 0x80) & 0xFF);
                            b >>>= 7;
                            if (b > 0x7F) {
                                data[position++] = (byte) ((b | 0x80) & 0xFF);
                                b >>>= 7;
                                if (b > 0x7F) {
                                    data[position++] = (byte) ((b | 0x80) & 0xFF);
                                    b >>>= 7;
                                    if (b > 0x7F) {
                                        data[position++] = (byte) ((b | 0x80) & 0xFF);
                                        b >>>= 7;
                                        if (b > 0x7F) {
                                            data[position++] = (byte) ((b | 0x80) & 0xFF);
                                            b >>>= 7;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        data[position++] = (byte) b;
        setLimit();
        return this;
    }

    public long readUlong() {
        int b = readByte() & 0xff;
        int n = b & 0x7f;
        long l;
        if (b > 0x7f) {
            b = readByte() & 0xff;
            n ^= (b & 0x7f) << 7;
            if (b > 0x7f) {
                b = readByte() & 0xff;
                n ^= (b & 0x7f) << 14;
                if (b > 0x7f) {
                    b = readByte() & 0xff;
                    n ^= (b & 0x7f) << 21;
                    if (b > 0x7f) {
                        l = innerLongDecode((long) n);
                    } else {
                        l = n;
                    }
                } else {
                    l = n;
                }
            } else {
                l = n;
            }
        } else {
            l = n;
        }
        return l;
    }

    public ByteBuilder putSlong(long b) {
        b = (b << 1) ^ (b >> 63);
        return putUlong(b);
    }

    public long readSlong() {
        long l = readUlong();
        return (l >>> 1) ^ -(l & 1);
    }

    private long innerLongDecode(long l) {
        int b = readByte() & 0xff;
        l ^= (b & 0x7fL) << 28;
        if (b > 0x7f) {
            b = readByte() & 0xff;
            l ^= (b & 0x7fL) << 35;
            if (b > 0x7f) {
                b = readByte() & 0xff;
                l ^= (b & 0x7fL) << 42;
                if (b > 0x7f) {
                    b = readByte() & 0xff;
                    l ^= (b & 0x7fL) << 49;
                    if (b > 0x7f) {
                        b = readByte() & 0xff;
                        l ^= (b & 0x7fL) << 56;
                        if (b > 0x7f) {
                            b = readByte() & 0xff;
                            l ^= (b & 0x7fL) << 63;
                            if (b > 0x7f) {
                                throw Sugar.makeThrow("Invalid long encoding");
                            }
                        }
                    }
                }
            }
        }
        return l;
    }

    public ByteBuilder putFloat(float b) {
        return putInt(Float.floatToRawIntBits(b));
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public ByteBuilder putDouble(double b) {
        return putLong(Double.doubleToRawLongBits(b));
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public ByteBuilder putStringWithTag(String str) {
        byte[] bytes = new byte[0];
        try {
            bytes = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ensureBounds(bytes.length + 1);
        System.arraycopy(bytes, 0, data, position, bytes.length);
        position += bytes.length;
        data[position++] = 0x00;
        setLimit();
        return this;
    }

    public String readStringWithTag() {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int v = readByte() & 0xff;
            if (v == 0)
                break;
            sb.append((char) v);
        }
        return sb.toString();
    }

    public byte[] toBytes() {
        return Arrays.copyOf(data, limit);
    }

    public int getLimit() {
        return limit;
    }

    public int getPosition() {
        return position;
    }

    public ByteBuilder setPosition(int position) {
        if (position >= 0 && position <= limit) {
            this.position = position;
            return this;
        } else {
            throw Sugar.makeThrow("Access exceeds limit");
        }

    }

    public int getReadableCount() {
        return limit - position;
    }

    public int getArrayLength(int fromPosition, int toPosition, int fixLength) {
        if (fixLength > 0) {
            return (toPosition - fromPosition) / fixLength;
        } else {
            int count = 0;
            for (int i = fromPosition; i < toPosition; i++) {
                if ((data[i] & ~0x7F) == 0) {
                    count++;
                }
            }
            return count;
        }
    }

    @Override
    public String toString() {
        return Strings.bytesToBinString(toBytes());
    }

    private void ensureBounds(int len) {
        if (position + len > capacity) {
            expand(len);
        }
    }

    private void checkBounds(int len) {
        if (position + len > limit) {
            throw Sugar.makeThrow("Access exceeds limit");
        }
    }

    private void expand(int addition) {
        while (limit + addition > capacity) {
            int newCapacity = Math.max(capacity * 2, 1);
            this.data = Arrays.copyOf(data, newCapacity);
            this.capacity = newCapacity;
        }
    }

    private void setLimit() {
        if (limit < position) {
            limit = position;
        }
    }
}