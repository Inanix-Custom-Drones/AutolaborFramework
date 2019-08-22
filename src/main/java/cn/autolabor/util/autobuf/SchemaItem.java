package cn.autolabor.util.autobuf;

import java.util.Objects;

public class SchemaItem {

    private String key;
    private DecodeType decodeType;
    private EncodeType encodeType;
    private String embeddedRef;
    public SchemaItem() {
    }
    public SchemaItem(String key, DecodeType decodeType, EncodeType encodeType, String embeddedRef) {
        this.key = key;
        this.decodeType = decodeType;
        this.encodeType = encodeType;
        this.embeddedRef = embeddedRef;
    }

    public byte getTypeCode() {
        return (byte) ((decodeType.getTypeNum() << 3) | (encodeType.getTypeNum() & 0x07));
    }

    public void setTypeCode(byte code) {
        this.decodeType = DecodeType.getType((byte) ((code & 0xF8) >>> 3));
        this.encodeType = EncodeType.getType((byte) (code & 0x07));
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public DecodeType getDecodeType() {
        return decodeType;
    }

    public void setDecodeType(DecodeType decodeType) {
        this.decodeType = decodeType;
    }

    public EncodeType getEncodeType() {
        return encodeType;
    }

    public void setEncodeType(EncodeType encodeType) {
        this.encodeType = encodeType;
    }

    public String getEmbeddedRef() {
        return embeddedRef;
    }

    public void setEmbeddedRef(String embeddedRef) {
        this.embeddedRef = embeddedRef;
    }

    public boolean checkEqual(String key, DecodeType decodeType, EncodeType encodeType, String ref) {
        return Objects.equals(this.key, key) && this.decodeType == decodeType && this.encodeType == encodeType && Objects.equals(this.embeddedRef, ref);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SchemaItem that = (SchemaItem) o;

        if (key != null ? !key.equals(that.key) : that.key != null)
            return false;
        if (decodeType != that.decodeType)
            return false;
        if (encodeType != that.encodeType)
            return false;
        return embeddedRef != null ? embeddedRef.equals(that.embeddedRef) : that.embeddedRef == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + decodeType.hashCode();
        result = 31 * result + encodeType.hashCode();
        result = 31 * result + (embeddedRef != null ? embeddedRef.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SchemaItem{" + "key='" + key + '\'' + ", decodeType=" + decodeType + ", encodeType=" + encodeType + ", embeddedRef='" + embeddedRef + '\'' + '}';
    }

    public enum EncodeType {
        VARINT((byte) 0x00), BIT64((byte) 0x01), LENGTH_DELIMITED((byte) 0x02), BIT32((byte) 0x05), BIT8((byte) 0x03), BIT16((byte) 0x04);

        private byte typeNum;

        EncodeType(byte type) {
            this.typeNum = type;
        }

        public static EncodeType getType(byte num) {
            for (EncodeType c : EncodeType.values()) {
                if (c.getTypeNum() == num) {
                    return c;
                }
            }
            return null;
        }

        public static EncodeType getFromDecodeType(DecodeType decodeType) {
            if (decodeType == DecodeType.BOOLEAN
                    || decodeType == DecodeType.UINT32
                    || decodeType == DecodeType.SINT32
                    || decodeType == DecodeType.UINT64
                    || decodeType == DecodeType.SINT64) {
                return VARINT;
            } else if (decodeType == DecodeType.INT64
                    || decodeType == DecodeType.DOUBLE) {
                return BIT64;
            } else if (decodeType == DecodeType.STRING
                    || decodeType == DecodeType.EMBEDDED
                    || decodeType == DecodeType.LIST
                    || decodeType == DecodeType.ARRAY
                    || decodeType == DecodeType.MAP) {
                return LENGTH_DELIMITED;
            } else if (decodeType == DecodeType.INT32
                    || decodeType == DecodeType.FLOAT) {
                return BIT32;
            } else if (decodeType == DecodeType.BYTE) {
                return BIT8;
            } else if (decodeType == DecodeType.INT16
                    || decodeType == DecodeType.UINT16
                    || decodeType == DecodeType.CHAR) {
                return BIT16;
            }
            return null;
        }

        public byte getTypeNum() {
            return typeNum;
        }
    }

    public enum DecodeType {
        BOOLEAN((byte) 0x00), BYTE((byte) 0x01),
        INT16((byte) 0x02), UINT16((byte) 0x03), CHAR((byte) 0x11),
        INT32((byte) 0x04), UINT32((byte) 0x05), SINT32((byte) 0x06),
        INT64((byte) 0x07), UINT64((byte) 0x08), SINT64((byte) 0x09),
        FLOAT((byte) 0x0A), DOUBLE((byte) 0x0B),
        STRING((byte) 0x0C),
        EMBEDDED((byte) 0x0D),
        LIST((byte) 0x0E), ARRAY((byte) 0x0F),
        MAP((byte) 0x10);

        private byte typeNum;

        DecodeType(byte typeNum) {
            this.typeNum = typeNum;
        }

        public static DecodeType getType(byte num) {
            for (DecodeType c : DecodeType.values()) {
                if (c.getTypeNum() == num) {
                    return c;
                }
            }
            return null;
        }

        public byte getTypeNum() {
            return typeNum;
        }
    }
}
