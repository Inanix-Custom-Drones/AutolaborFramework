package cn.autolabor.module.communication;

public enum TCPRespStatusType {

    SUCCESS((byte) 0x00), FAILURE((byte) 0xFF), NO_TASK((byte) 0xFE), NO_METHOD((byte) 0xFC), ILLEGAL_ARGUMENT((byte) 0xFB),
    NO_TCP_SUPPORT((byte) 0xFA), NO_DEVICE((byte) 0xF9);

    private byte code;

    TCPRespStatusType(byte code) {
        this.code = code;
    }

    public static TCPRespStatusType getTypeFromCode(byte code) {
        for (TCPRespStatusType type : TCPRespStatusType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    public byte getCode() {
        return code;
    }
}
