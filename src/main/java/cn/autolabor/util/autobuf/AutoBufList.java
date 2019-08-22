package cn.autolabor.util.autobuf;

import cn.autolabor.message.navigation.Msg2DTwist;
import cn.autolabor.util.Sugar;
import cn.autolabor.util.reflect.Reflects;
import cn.autolabor.util.reflect.TypeFetch;
import cn.autolabor.util.reflect.TypeNode;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class AutoBufList extends AutoBufObject {

    private SchemaItem.DecodeType subItemType;

    AutoBufList(String key, SchemaItem.DecodeType subItemType) {
        super(key, SchemaItem.DecodeType.LIST);
        this.data = new ArrayList<AutoBufObject>();
        this.subItemType = subItemType;
    }

    AutoBufList(String key, Object object) {
        super(key, SchemaItem.DecodeType.LIST);
        this.data = new ArrayList<AutoBufObject>();
        if (object instanceof List) {
            List rawList = (List) object;
            if (checkConsistency(rawList)) {
                if (rawList.size() > 0) {
                    Object subItem = rawList.get(0);
                    this.subItemType = getDefaultDecodeTypeFromObject(subItem);
                    if (this.subItemType != null) {
                        for (Object obj : rawList) {
                            ((ArrayList<AutoBufObject>) data).add(createObject(null, obj, subItemType));
                        }
                    } else {
                        throw Sugar.makeThrow("List data type is inconsistent");
                    }
                }
            } else {
                throw Sugar.makeThrow("List data type is inconsistent");
            }
        } else {
            throw Sugar.makeThrow("Object is not list data");
        }

    }

    public static void main(String[] args) {
        List data = new ArrayList();
        data.add(new Msg2DTwist(1, 2, 3));
        data.add(new Msg2DTwist(2, 3, 4));

        AutoBufList autoBufList = new AutoBufList(null, data);
        System.out.println(autoBufList.toRawData(new TypeFetch<List<Msg2DTwist>>() {
        }.getTypeNode()));

    }

    public AutoBufList addBoolean(boolean b) {
        if (subItemType.equals(SchemaItem.DecodeType.BOOLEAN)) {
            ((List<AutoBufObject>) data).add(new AutoBufObject(null, b, SchemaItem.DecodeType.BOOLEAN));
            return this;
        } else {
            throw Sugar.makeThrow("Unable to add boolean type variable");
        }
    }

    public AutoBufList addByte(byte b) {
        if (subItemType.equals(SchemaItem.DecodeType.BYTE)) {
            ((List<AutoBufObject>) data).add(new AutoBufObject(null, b, SchemaItem.DecodeType.BYTE));
            return this;
        } else {
            throw Sugar.makeThrow("Unable to add byte type variable");
        }
    }

    public AutoBufList addChar(char c) {
        if (subItemType.equals(SchemaItem.DecodeType.CHAR)) {
            ((List<AutoBufObject>) data).add(new AutoBufObject(null, c, SchemaItem.DecodeType.CHAR));
            return this;
        } else {
            throw Sugar.makeThrow("Unable to add char type variable");
        }
    }

    public AutoBufList addShort(short s) {
        if (subItemType.equals(SchemaItem.DecodeType.INT16) || subItemType.equals(SchemaItem.DecodeType.UINT16)) {
            ((List<AutoBufObject>) data).add(new AutoBufObject(null, s, subItemType));
            return this;
        } else {
            throw Sugar.makeThrow("Unable to add short type variable");
        }
    }

    public AutoBufList addInt(int i) {
        if (subItemType.equals(SchemaItem.DecodeType.INT32) || subItemType.equals(SchemaItem.DecodeType.UINT32) || subItemType.equals(SchemaItem.DecodeType.SINT32)) {
            ((List<AutoBufObject>) data).add(new AutoBufObject(null, i, subItemType));
            return this;
        } else {
            throw Sugar.makeThrow("Unable to add int type variable");
        }
    }

    public AutoBufList addLong(long l) {
        if (subItemType.equals(SchemaItem.DecodeType.INT64) || subItemType.equals(SchemaItem.DecodeType.UINT64) || subItemType.equals(SchemaItem.DecodeType.SINT64)) {
            ((List<AutoBufObject>) data).add(new AutoBufObject(null, l, subItemType));
            return this;
        } else {
            throw Sugar.makeThrow("Unable to add long type variable");
        }
    }

    public AutoBufList addFloat(float f) {
        if (subItemType.equals(SchemaItem.DecodeType.FLOAT)) {
            ((List<AutoBufObject>) data).add(new AutoBufObject(null, f, subItemType));
            return this;
        } else {
            throw Sugar.makeThrow("Unable to add float type variable");
        }
    }

    public AutoBufList addDouble(double d) {
        if (subItemType.equals(SchemaItem.DecodeType.DOUBLE)) {
            ((List<AutoBufObject>) data).add(new AutoBufObject(null, d, subItemType));
            return this;
        } else {
            throw Sugar.makeThrow("Unable to add double type variable");
        }
    }

    public AutoBufList addString(String str) {
        if (subItemType.equals(SchemaItem.DecodeType.STRING)) {
            ((List<AutoBufObject>) data).add(new AutoBufObject(null, str, SchemaItem.DecodeType.STRING));
            return this;
        } else {
            throw Sugar.makeThrow("Unable to add string type variable");
        }
    }

    public AutoBufList addList(List list) {
        if (subItemType.equals(SchemaItem.DecodeType.LIST)) {
            ((List<AutoBufObject>) data).add(new AutoBufList(null, list));
            return this;
        } else {
            throw Sugar.makeThrow("Unable to add List type variable");
        }
    }

    public AutoBufList addArray(Object array) {
        if (subItemType.equals(SchemaItem.DecodeType.ARRAY)) {
            ((List<AutoBufObject>) data).add(new AutoBufArray(null, array));
            return this;
        } else {
            throw Sugar.makeThrow("Unable to add Array type variable");
        }
    }

    public AutoBufList addMap(Object map) {
        if (subItemType.equals(SchemaItem.DecodeType.MAP)) {
            ((List<AutoBufObject>) data).add(new AutoBufMap(null, map));
            return this;
        } else {
            throw Sugar.makeThrow("Unable to add Map type variable");
        }
    }

    public AutoBufList addEmbedded(Object embedded) {
        if (subItemType.equals(SchemaItem.DecodeType.EMBEDDED)) {
            ((List<AutoBufObject>) data).add(new AutoBufEmbedded(null, (SerializableMessage) embedded));
            return this;
        } else {
            throw Sugar.makeThrow("Unable to add Embedded type variable");
        }
    }

    public AutoBufList add(AutoBufObject object) {
        if (object.getType() == subItemType) {
            ((List<AutoBufObject>) data).add(object);
            return this;
        } else {
            throw Sugar.makeThrow("Unable to add %s type variable", object.getType().name());
        }
    }

    @Override
    public Object toRawData(TypeNode typeNode) {
        if (typeNode.isParameterized()) {
            Class classType = (Class) ((ParameterizedType) typeNode.getType()).getRawType();
            if (Reflects.isList(classType)) {
                List<AutoBufObject> list = (List<AutoBufObject>) data;
                List rawData = Sugar.createListByType(classType);
                TypeNode subTypeNode = typeNode.getArguments(0);
                for (int i = 0; i < list.size(); i++) {
                    rawData.add(list.get(i).toRawData(subTypeNode));
                }
                return rawData;
            }
        }
        throw Sugar.makeThrow("Data type does not match");
    }

    public ArrayList<AutoBufObject> getData() {
        return ((ArrayList<AutoBufObject>) data);
    }

    public SchemaItem.DecodeType getSubItemType() {
        return subItemType;
    }

}
