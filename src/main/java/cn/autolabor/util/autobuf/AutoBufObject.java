package cn.autolabor.util.autobuf;

import cn.autolabor.util.Sugar;
import cn.autolabor.util.reflect.Reflects;
import cn.autolabor.util.reflect.TypeNode;

import java.util.Collection;

public class AutoBufObject {

    protected String key;
    protected Object data;
    protected SchemaItem.DecodeType type;

    protected String tmpUri;

    AutoBufObject(String key, SchemaItem.DecodeType type) {
        this.key = key;
        this.type = type;
    }

    AutoBufObject(String key, Object data, SchemaItem.DecodeType type) {
        this.key = key;
        this.data = data;
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    static AutoBufObject createObject(String key, Object obj, SchemaItem.DecodeType type) {
        if (type == SchemaItem.DecodeType.BOOLEAN || type == SchemaItem.DecodeType.BYTE
                || type == SchemaItem.DecodeType.CHAR || type == SchemaItem.DecodeType.INT16
                || type == SchemaItem.DecodeType.UINT16 || type == SchemaItem.DecodeType.INT32
                || type == SchemaItem.DecodeType.UINT32 || type == SchemaItem.DecodeType.SINT32
                || type == SchemaItem.DecodeType.INT64 || type == SchemaItem.DecodeType.UINT64
                || type == SchemaItem.DecodeType.SINT64 || type == SchemaItem.DecodeType.FLOAT
                || type == SchemaItem.DecodeType.DOUBLE || type == SchemaItem.DecodeType.STRING) {
            return new AutoBufObject(key, obj, type);
        } else if (type == SchemaItem.DecodeType.EMBEDDED) {
            return new AutoBufEmbedded(key, (SerializableMessage) obj);
        } else if (type == SchemaItem.DecodeType.LIST) {
            return new AutoBufList(key, obj);
        } else if (type == SchemaItem.DecodeType.ARRAY) {
            return new AutoBufArray(key, obj);
        } else if (type == SchemaItem.DecodeType.MAP) {
            return new AutoBufMap(key, obj);
        } else {
            throw Sugar.makeThrow("Unable to parse data %s", obj.toString());
        }
    }

    static boolean checkConsistency(Collection data) {
        Class type = null;
        for (Object subItem : data) {
            if (type == null) {
                type = subItem.getClass();
            }

            if (type != subItem.getClass()) {
                return false;
            }
        }
        return true;
    }

    static SchemaItem.DecodeType getDefaultDecodeTypeFromObject(Object data) {
        Class dataType = data.getClass();
        if (Reflects.isBoolean(dataType)) {
            return SchemaItem.DecodeType.BOOLEAN;
        } else if (Reflects.isByte(dataType)) {
            return SchemaItem.DecodeType.BYTE;
        } else if (Reflects.isChar(dataType)) {
            return SchemaItem.DecodeType.CHAR;
        } else if (Reflects.isShort(dataType)) {
            return SchemaItem.DecodeType.INT16;
        } else if (Reflects.isInt(dataType)) {
            return SchemaItem.DecodeType.SINT32;
        } else if (Reflects.isLong(dataType)) {
            return SchemaItem.DecodeType.SINT64;
        } else if (Reflects.isFloat(dataType)) {
            return SchemaItem.DecodeType.FLOAT;
        } else if (Reflects.isDouble(dataType)) {
            return SchemaItem.DecodeType.DOUBLE;
        } else if (Reflects.isString(dataType)) {
            return SchemaItem.DecodeType.STRING;
        } else if (Reflects.isArray(dataType)) {
            return SchemaItem.DecodeType.ARRAY;
        } else if (Reflects.isList(dataType)) {
            return SchemaItem.DecodeType.LIST;
        } else if (Reflects.isMap(dataType)) {
            return SchemaItem.DecodeType.MAP;
        } else if (Sugar.checkInherit(dataType, SerializableMessage.class)) {
            return SchemaItem.DecodeType.EMBEDDED;
        }
        return null;
    }

    public Object toRawData(TypeNode type) {
        return data;
    }

    public SchemaItem.DecodeType getType() {
        return type;
    }

    public void setType(SchemaItem.DecodeType type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public Object getData() {
        return data;
    }

    public String getTmpUri() {
        return tmpUri;
    }

    public void setTmpUri(String tmpUri) {
        this.tmpUri = tmpUri;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
