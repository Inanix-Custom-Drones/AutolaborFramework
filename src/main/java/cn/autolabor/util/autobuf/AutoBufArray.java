package cn.autolabor.util.autobuf;

import cn.autolabor.message.navigation.Msg2DTwist;
import cn.autolabor.util.Sugar;
import cn.autolabor.util.reflect.TypeFetch;
import cn.autolabor.util.reflect.TypeNode;

import java.util.ArrayList;
import java.util.Arrays;

public class AutoBufArray extends AutoBufObject {

    //    private Object data;
    private SchemaItem.DecodeType subItemType;

    AutoBufArray(String key, Object data, SchemaItem.DecodeType subItemType) {
        super(key, SchemaItem.DecodeType.ARRAY);
        this.data = data;
        this.subItemType = subItemType;
    }

    AutoBufArray(String key, Object obj) {
        super(key, SchemaItem.DecodeType.ARRAY);
        if (obj instanceof boolean[]) {
            this.subItemType = SchemaItem.DecodeType.BOOLEAN;
            data = obj;
        } else if (obj instanceof byte[]) {
            this.subItemType = SchemaItem.DecodeType.BYTE;
            data = obj;
        } else if (obj instanceof short[]) {
            this.subItemType = SchemaItem.DecodeType.INT16;
            data = obj;
        } else if (obj instanceof char[]) {
            this.subItemType = SchemaItem.DecodeType.CHAR;
            data = obj;
        } else if (obj instanceof int[]) {
            this.subItemType = SchemaItem.DecodeType.SINT32;
            data = obj;
        } else if (obj instanceof long[]) {
            this.subItemType = SchemaItem.DecodeType.SINT64;
            data = obj;
        } else if (obj instanceof float[]) {
            this.subItemType = SchemaItem.DecodeType.FLOAT;
            data = obj;
        } else if (obj instanceof double[]) {
            this.subItemType = SchemaItem.DecodeType.DOUBLE;
            data = obj;
        } else if (obj instanceof Object[]) {
            Object[] objects = (Object[]) obj;
            if (objects.length > 0) {
                Object subItem = objects[0];
                this.subItemType = getDefaultDecodeTypeFromObject(subItem);
                if (this.subItemType != null) {
                    ArrayList<AutoBufObject> array = new ArrayList<>();
                    for (Object subObj : objects) {
                        array.add(createObject(null, subObj, subItemType));
                    }
                    data = array;
                } else {
                    throw Sugar.makeThrow("The Array data cannot be converted");
                }
            }
        } else {
            throw Sugar.makeThrow("Object is not Array!");
        }
    }

    public static void main(String[] args) {
        Msg2DTwist[] msg2DTwists = new Msg2DTwist[2];
        msg2DTwists[0] = new Msg2DTwist(1, 2, 3);
        msg2DTwists[1] = new Msg2DTwist(1, 2, 3);

        AutoBufArray autoBufArray = new AutoBufArray(null, msg2DTwists);
        TypeNode typeNode = new TypeFetch<Msg2DTwist[]>() {
        }.getTypeNode();
        System.out.println(typeNode);

        Object rawData = autoBufArray.toRawData(typeNode);
        System.out.println(Arrays.toString((Object[]) rawData));
    }

    public SchemaItem.DecodeType getSubItemType() {
        return subItemType;
    }

    @SuppressWarnings("unchecked")
    public Object toRawData(TypeNode typeNode) {
        if (typeNode.isArray()) {
            if (typeNode.getArguments(0).isPrimitive()) {
                return data;
            } else {
                ArrayList<AutoBufObject> array = (ArrayList<AutoBufObject>) data;
                Object[] rawData = new Object[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    AutoBufObject anArray = array.get(i);
                    rawData[i] = anArray.toRawData(typeNode.getArguments(0));
                }
                return rawData;
            }
        } else {
            throw Sugar.makeThrow("Data type does not match");
        }
    }

}
