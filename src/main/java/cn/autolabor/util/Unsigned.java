package cn.autolabor.util;

public class Unsigned {

    private static final int BYTE_CAPACITY_FOR_INT = 0xff;
    private static final int SHORT_CAPACITY_FOR_INT = 0xffff;
    private static final long BYTE_CAPACITY_FOR_LONG = 0xffL;
    private static final long SHORT_CAPACITY_FOR_LONG = 0xffffL;
    private static final long INT_CAPACITY_FOR_LONG = 0xffffffffL;

    public static byte int2UnsignedByte(int data) {
        return (byte) (data & BYTE_CAPACITY_FOR_INT);
    }

    public static int unsignedByte2int(byte data) {
        return data & BYTE_CAPACITY_FOR_INT;
    }

    public static short int2UnsignedShort(int data) {
        return (short) (data & SHORT_CAPACITY_FOR_INT);
    }

    public static int unsignedShort2Int(short data) {
        return data & SHORT_CAPACITY_FOR_INT;
    }

    public static byte long2UnsignedByte(long data) {
        return (byte) (data & BYTE_CAPACITY_FOR_LONG);
    }

    public static long UnsignedByte2Long(byte data) {
        return data & BYTE_CAPACITY_FOR_LONG;
    }

    public static short long2UnsignedShort(long data) {
        return (short) (data & SHORT_CAPACITY_FOR_LONG);
    }

    public static long UnsignedShort2Long(int data) {
        return data & SHORT_CAPACITY_FOR_LONG;
    }

    public static int long2UnsignedInt(long data) {
        return (int) (data & INT_CAPACITY_FOR_LONG);
    }

    public static long UnsignedInt2Long(int data) {
        return data & INT_CAPACITY_FOR_LONG;
    }

    public static void main(String[] args) {
        //        byte a0 = (byte) 0xff;
        //        byte a1 = (byte) 0x38;
        //
        //        short a = (short) (((a0 << 8) | a1));
        //        System.out.println(a);


        short b = -12547;
        System.out.println((byte) b);

        System.out.println((byte) ((b) >>> 8));
        System.out.println((byte) (b));
    }

}
