package cn.autolabor.util.autobuf;

import cn.autolabor.util.Sugar;

public final class AutoBufBuilder {

    public static AutoBufEmbedded createEmbedded(String key, String id) {
        // TODO: 校验id不为空
        return new AutoBufEmbedded(key, id);
    }

    public static AutoBufList createList(SchemaItem.DecodeType subItemType) {
        return new AutoBufList(null, subItemType);
    }

    public static AutoBufList createList(String key, SchemaItem.DecodeType subItemType) {
        return new AutoBufList(key, subItemType);
    }

    public static AutoBufArray createArray(SchemaItem.DecodeType subItemType) {
        return new AutoBufArray(null, subItemType);
    }

    public static AutoBufArray createArray(String key, SchemaItem.DecodeType subItemType) {
        return new AutoBufArray(key, subItemType);
    }

    public static AutoBufMap createMap(SchemaItem.DecodeType keyType, SchemaItem.DecodeType valueType) {
        return new AutoBufMap(null, keyType, valueType);
    }

    public static AutoBufMap createMap(String key, SchemaItem.DecodeType keyType, SchemaItem.DecodeType valueType) {
        return new AutoBufMap(key, keyType, valueType);
    }

    public static AutoBufObject createFromObject(String key, Object object) {
        SchemaItem.DecodeType type = AutoBufObject.getDefaultDecodeTypeFromObject(object);
        if (type != null) {
            return AutoBufObject.createObject(key, object, type);
        } else {
            throw Sugar.makeThrow("Unable to create autobufobject from object");
        }
    }


}
