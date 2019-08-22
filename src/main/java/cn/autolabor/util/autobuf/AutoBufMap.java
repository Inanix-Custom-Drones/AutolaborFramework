package cn.autolabor.util.autobuf;

import cn.autolabor.util.Sugar;
import cn.autolabor.util.reflect.Reflects;
import cn.autolabor.util.reflect.TypeNode;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class AutoBufMap extends AutoBufObject {

    //    private HashMap<AutoBufObject, AutoBufObject> data = new HashMap<>();
    private SchemaItem.DecodeType keyType;
    private SchemaItem.DecodeType valueType;

    AutoBufMap(String key, SchemaItem.DecodeType keyType, SchemaItem.DecodeType valueType) {
        super(key, SchemaItem.DecodeType.MAP);
        data = new HashMap<AutoBufObject, AutoBufObject>();
        this.keyType = keyType;
        this.valueType = valueType;
    }

    AutoBufMap(String key, Object object) {
        super(key, SchemaItem.DecodeType.MAP);
        data = new HashMap<AutoBufObject, AutoBufObject>();
        if (object instanceof Map) {
            Map map = (Map) object;
            int count = map.size();
            Collection keys = map.keySet();
            Collection values = map.values();
            if (checkConsistency(keys) && checkConsistency(values)) {
                if (count > 0) {
                    this.keyType = getDefaultDecodeTypeFromObject(keys.iterator().next());
                    this.valueType = getDefaultDecodeTypeFromObject(values.iterator().next());
                    for (Object entry : map.entrySet()) {
                        ((Map<AutoBufObject, AutoBufObject>) data).put(createObject(null, ((Map.Entry) entry).getKey(), keyType), createObject(null, ((Map.Entry) entry).getValue(), valueType));
                    }
                }
            } else {
                throw Sugar.makeThrow("Map data key or values is inconsistent");
            }
        } else {
            throw Sugar.makeThrow("Object is not map data");
        }
    }

    public AutoBufMap putRawData(Object key, Object value) {
        SchemaItem.DecodeType keyType = getDefaultDecodeTypeFromObject(key);
        SchemaItem.DecodeType valueType = getDefaultDecodeTypeFromObject(value);
        if (keyType != null && valueType != null && this.keyType == keyType && this.valueType == valueType) {
            ((Map<AutoBufObject, AutoBufObject>) data).put(createObject(null, key, keyType), createObject(null, value, valueType));
            return this;
        } else {
            throw Sugar.makeThrow("The type of key or value does not match the specified type");
        }
    }

    public AutoBufMap put(AutoBufObject key, AutoBufObject value) {
        if (key.getType() == this.keyType && value.getType() == this.valueType && this.keyType != null && this.valueType != null) {
            ((Map<AutoBufObject, AutoBufObject>) data).put(key, value);
            return this;
        } else {
            throw Sugar.makeThrow("The type of key or value does not match the specified type");
        }
    }

    @Override
    public Object toRawData(TypeNode typeNode) {
        if (typeNode.isParameterized()) {
            Class classType = (Class) ((ParameterizedType) typeNode.getType()).getRawType();
            if (Reflects.isMap(classType) && typeNode.getArguments() != null && typeNode.getArguments().length == 2) {
                TypeNode keyType = typeNode.getArguments(0);
                TypeNode valueType = typeNode.getArguments(1);
                Map<AutoBufObject, AutoBufObject> map = (Map<AutoBufObject, AutoBufObject>) data;
                Map rawMap = Sugar.createMapByType(classType);
                for (Map.Entry<AutoBufObject, AutoBufObject> entry : map.entrySet()) {
                    rawMap.put(entry.getKey().toRawData(keyType), entry.getValue().toRawData(valueType));
                }
                return rawMap;
            }
        }
        throw Sugar.makeThrow("Data type does not match");
    }

    public SchemaItem.DecodeType getKeyType() {
        return keyType;
    }

    public SchemaItem.DecodeType getValueType() {
        return valueType;
    }

}
