package cn.autolabor.util.autobuf;

import cn.autolabor.message.navigation.Msg2DOdometry;
import cn.autolabor.util.Strings;
import cn.autolabor.util.Sugar;
import cn.autolabor.util.Unsafes;
import cn.autolabor.util.autobuf.annotation.IgnoreField;
import cn.autolabor.util.reflect.Reflects;
import cn.autolabor.util.reflect.TypeFetch;
import cn.autolabor.util.reflect.TypeNode;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class AutoBufAdapter {

    TypeNode adapterType;
    Map<Integer, SchemaItem> fieldItemMap = new LinkedHashMap<>(); // ID -- ITEM
    Map<Integer, Field> fieldLink = new TreeMap<>();
    Map<String, TreeSet<Integer>> refFieldMap = new LinkedHashMap<>(); // REF -- IDS
    Map<String, Class> refLink = new LinkedHashMap<>();
    byte[] headerCache;
    int headerHash;
    Map<String, String> tmpRefMap = new HashMap<>(); // CLASSNAME -- REF
    private int startSeq = 1;
    private AtomicInteger seq = new AtomicInteger(startSeq);
    private AtomicInteger refSeq = new AtomicInteger(0);

    public AutoBufAdapter(Type type) {
        this(new TypeNode(type));
    }

    public AutoBufAdapter(TypeNode typeNode) {
        this.adapterType = typeNode;
        create(typeNode, null, generateRef());
        encodeHeader();
    }

    public static void main(String[] args) {

        AutoBufAdapter adapter = new AutoBufAdapter(new TypeFetch<ArrayList<Msg2DOdometry>>() {
        }.getTypeNode());
        System.out.println(adapter);


        //        AutoBufAdapter adapter = new AutoBufAdapter(new TypeNode(Msg2DPose.class));
        //        System.out.println(adapter);
        //
        //        Msg2DPose pose = new Msg2DPose(0.41, 1.22, 2.13, "map");
        //        byte[] adapterBytes = adapter.encode(pose);
        //        System.out.println("adapter-encoder : " + Strings.bytesToHexString(adapterBytes));
        //        System.out.println("adapter-decoder : " + adapter.decode(adapterBytes));
        //
        //        byte[] builderBytes = AutoBufEncoder.toBytes(AutoBufBuilder.createFromObject(null, pose));
        //        System.out.println("builder-encoder : " + Strings.bytesToHexString(builderBytes));
        //
        //        AutoBufObject object = AutoBufBuilder.createEmbedded(null, "pose").putRawData("y", 1.22).putRawData("x", 0.41)
        //                .putRawData("coordinate", "map").putRawData("yaw", 2.13);
        //        byte[] freeBytes = AutoBufEncoder.toBytes(object);
        //        System.out.println("free-encoder :    " + Strings.bytesToHexString(freeBytes));
        //
        //        System.out.println(adapter.decode(freeBytes));
    }

    public byte[] getHeader() {
        return headerCache;
    }

    public byte[] encodeBody(Object msg) {
        return encodeMessage(msg, fieldItemMap.get(startSeq), startSeq, true).toBytes();
    }

    public Object decodeBody(byte[] bodyBytes) {
        ByteBuilder bb = new ByteBuilder(bodyBytes).resetPosition();
        int bodyHeader = bb.readUint();
        int readFieldId = (bodyHeader >>> 3);
        int endPosition = 0;
        SchemaItem.EncodeType encodeType = SchemaItem.EncodeType.getType((byte) (bodyHeader & 0x07));
        if (Objects.equals(encodeType, SchemaItem.EncodeType.LENGTH_DELIMITED)) {
            endPosition = bb.readUint() + bb.getPosition();
        }
        return decodeMessage(bb, readFieldId, endPosition);
    }

    public Object decodeBody(ByteBuilder bb) {
        int bodyHeader = bb.readUint();
        int readFieldId = (bodyHeader >>> 3);
        int endPosition = 0;
        SchemaItem.EncodeType encodeType = SchemaItem.EncodeType.getType((byte) (bodyHeader & 0x07));
        if (Objects.equals(encodeType, SchemaItem.EncodeType.LENGTH_DELIMITED)) {
            endPosition = bb.readUint() + bb.getPosition();
        }
        return decodeMessage(bb, readFieldId, endPosition);
    }

    public byte[] encode(Object msg) {
        ByteBuilder bb = encodeMessage(msg, fieldItemMap.get(startSeq), startSeq, true);
        return new ByteBuilder(headerCache).putBytes(bb).toBytes();
    }

    public Object decode(byte[] bytes) {
        return decode(new ByteBuilder(bytes).resetPosition());
    }

    public Object decode(ByteBuilder bb) {
        int currentPosition = bb.getPosition();
        int headerLen = bb.readUint() + bb.getPosition();
        byte[] header = bb.resetPosition().setPosition(currentPosition).readBytes(headerLen);
        int hash = Arrays.hashCode(header);
        if (hash != headerHash) {
            this.headerCache = header;
            this.headerHash = hash;
            this.decodeHeader();
            this.link();
        }

        int bodyHeader = bb.readUint();
        int readFieldId = (bodyHeader >>> 3);
        int endPosition = 0;
        SchemaItem.EncodeType encodeType = SchemaItem.EncodeType.getType((byte) (bodyHeader & 0x07));
        if (Objects.equals(encodeType, SchemaItem.EncodeType.LENGTH_DELIMITED)) {
            endPosition = bb.readUint() + bb.getPosition();
        }
        return decodeMessage(bb, readFieldId, endPosition);
    }

    public byte[] getBody(byte[] code) {
        ByteBuilder bb = new ByteBuilder(code).resetPosition();
        int headerLen = bb.readUint() + bb.getPosition();
        byte[] header = bb.resetPosition().readBytes(headerLen);
        byte[] body = bb.readBytes(bb.getReadableCount());
        return body;
    }

    private Object decodeString(ByteBuilder bb, Integer endPosition) {
        try {
            return new String(bb.readBytes(endPosition - bb.getPosition()), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object decodeEmbedded(ByteBuilder bb, SchemaItem item, Integer endPosition) {
        Object msg;
        try {
            msg = Unsafes.allocateInstance(refLink.get(item.getEmbeddedRef()));
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw Sugar.makeThrow("Instantiated object failed");
        }

        while (bb.getPosition() < endPosition) {
            int header = bb.readUint();
            int readFieldId = (header >>> 3);
            SchemaItem.EncodeType encodeType = SchemaItem.EncodeType.getType((byte) (header & 0x07));
            int subEndposition = 0;
            if (encodeType != null && encodeType.equals(SchemaItem.EncodeType.LENGTH_DELIMITED)) {
                subEndposition = bb.readUint() + bb.getPosition();
            }
            Object subMsg = decodeMessage(bb, readFieldId, subEndposition);
            Field f = fieldLink.get(readFieldId);
            try {
                f.set(msg, subMsg);
            } catch (IllegalAccessException e) {
                throw Sugar.makeThrow("Field set failed");
            }
        }
        return msg;
    }

    @SuppressWarnings("unchecked")
    private Object decodeList(ByteBuilder bb, SchemaItem item, Integer endPosition) {
        List<Object> list = Sugar.createListByType(refLink.get(item.getEmbeddedRef()));
        int subListFieldId = refFieldMap.get(item.getEmbeddedRef()).iterator().next();
        SchemaItem subListItem = fieldItemMap.get(subListFieldId);
        if (subListItem.getEncodeType().equals(SchemaItem.EncodeType.LENGTH_DELIMITED)) {
            while (bb.getPosition() < endPosition) {
                bb.readUint();
                int subListEndPosition = bb.readUint() + bb.getPosition();
                list.add(decodeMessage(bb, subListFieldId, subListEndPosition));
            }
        } else {
            while (bb.getPosition() < endPosition) {
                list.add(decodeMessage(bb, subListFieldId, 0));
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private Object decodeArray(ByteBuilder bb, SchemaItem item, Integer endPosition) {
        int subArrayFieldId = refFieldMap.get(item.getEmbeddedRef()).iterator().next();
        SchemaItem subArrayItem = fieldItemMap.get(subArrayFieldId);
        Class arrayType = refLink.get(item.getEmbeddedRef());
        if (!Sugar.checkInherit(arrayType, Object[].class)) {
            int arrayLength;
            switch (subArrayItem.getDecodeType()) {
                case BOOLEAN:
                    arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 1);
                    boolean[] booleans = new boolean[arrayLength];
                    for (int i = 0; i < arrayLength; i++) {
                        booleans[i] = bb.readBoolean();
                    }
                    return booleans;
                case BYTE:
                    arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 1);
                    byte[] bytes = new byte[arrayLength];
                    for (int i = 0; i < arrayLength; i++) {
                        bytes[i] = bb.readByte();
                    }
                    return bytes;
                case CHAR:
                    arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 2);
                    char[] chars = new char[arrayLength];
                    for (int i = 0; i < arrayLength; i++) {
                        chars[i] = bb.readChar();
                    }
                    return chars;
                case INT16:
                case UINT16:
                    arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 2);
                    short[] shorts = new short[arrayLength];
                    for (int i = 0; i < arrayLength; i++) {
                        shorts[i] = bb.readShort();
                    }
                    return shorts;
                case INT32:
                    arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 4);
                    int[] ints = new int[arrayLength];
                    for (int i = 0; i < arrayLength; i++) {
                        ints[i] = bb.readInt();
                    }
                    return ints;
                case UINT32:
                    arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 0);
                    int[] uints = new int[arrayLength];
                    for (int i = 0; i < arrayLength; i++) {
                        uints[i] = bb.readUint();
                    }
                    return uints;
                case SINT32:
                    arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 0);
                    int[] sints = new int[arrayLength];
                    for (int i = 0; i < arrayLength; i++) {
                        sints[i] = bb.readSint();
                    }
                    return sints;
                case INT64:
                    arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 8);
                    long[] longs = new long[arrayLength];
                    for (int i = 0; i < arrayLength; i++) {
                        longs[i] = bb.readLong();
                    }
                    return longs;
                case UINT64:
                    arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 0);
                    long[] ulongs = new long[arrayLength];
                    for (int i = 0; i < arrayLength; i++) {
                        ulongs[i] = bb.readUlong();
                    }
                    return ulongs;
                case SINT64:
                    arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 0);
                    long[] slongs = new long[arrayLength];
                    for (int i = 0; i < arrayLength; i++) {
                        slongs[i] = bb.readSlong();
                    }
                    return slongs;
                case FLOAT:
                    arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 4);
                    float[] floats = new float[arrayLength];
                    for (int i = 0; i < arrayLength; i++) {
                        floats[i] = bb.readFloat();
                    }
                    return floats;
                case DOUBLE:
                    arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 8);
                    double[] doubles = new double[arrayLength];
                    for (int i = 0; i < arrayLength; i++) {
                        doubles[i] = bb.readDouble();
                    }
                    return doubles;
            }
        } else {
            List<Object> array = new ArrayList<>();
            if (subArrayItem.getEncodeType().equals(SchemaItem.EncodeType.LENGTH_DELIMITED)) {
                while (bb.getPosition() < endPosition) {
                    bb.readUint();
                    int subArrayEndPosition = bb.readUint() + bb.getPosition();
                    array.add(decodeMessage(bb, subArrayFieldId, subArrayEndPosition));
                }
            } else {
                while (bb.getPosition() < endPosition) {
                    array.add(decodeMessage(bb, subArrayFieldId, 0));
                }
            }
            return Arrays.copyOf(array.toArray(), array.size(), refLink.get(item.getEmbeddedRef()));
        }
        return null;
    }

    private Object decodeMap(ByteBuilder bb, SchemaItem item, Integer endPosition) {
        Map<Object, Object> map = new HashMap<>();
        Object key, value;
        Iterator<Integer> iter = refFieldMap.get(item.getEmbeddedRef()).iterator();
        int keyFieldId = iter.next();
        SchemaItem keyItem = fieldItemMap.get(keyFieldId);
        int valueFieldId = iter.next();
        SchemaItem valueItem = fieldItemMap.get(valueFieldId);
        while (bb.getPosition() < endPosition) {
            if (keyItem.getEncodeType().equals(SchemaItem.EncodeType.LENGTH_DELIMITED)) {
                bb.readUint();
                int keyEndPosition = bb.readUint() + bb.getPosition();
                key = decodeMessage(bb, keyFieldId, keyEndPosition);
            } else {
                bb.readUint();
                key = decodeMessage(bb, keyFieldId, 0);
            }

            if (valueItem.getEncodeType().equals(SchemaItem.EncodeType.LENGTH_DELIMITED)) {
                bb.readUint();
                int valueEndPosition = bb.readUint() + bb.getPosition();
                value = decodeMessage(bb, valueFieldId, valueEndPosition);
            } else {
                bb.readUint();
                value = decodeMessage(bb, valueFieldId, 0);
            }
            map.put(key, value);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private Object decodeMessage(ByteBuilder bb, Integer fieldId, Integer endPosition) {
        SchemaItem item = fieldItemMap.get(fieldId);
        switch (item.getDecodeType()) {
            case BOOLEAN:
                return bb.readBoolean();
            case BYTE:
                return bb.readByte();
            case INT16:
                return bb.readShort();
            case UINT16:
                return bb.readShort();
            case CHAR:
                return bb.readChar();
            case INT32:
                return bb.readInt();
            case UINT32:
                return bb.readUint();
            case SINT32:
                return bb.readSint();
            case INT64:
                return bb.readLong();
            case UINT64:
                return bb.readUlong();
            case SINT64:
                return bb.readSlong();
            case FLOAT:
                return bb.readFloat();
            case DOUBLE:
                return bb.readDouble();
            case STRING:
                return decodeString(bb, endPosition);
            case EMBEDDED:
                return decodeEmbedded(bb, item, endPosition);
            case LIST:
                return decodeList(bb, item, endPosition);
            case ARRAY:
                return decodeArray(bb, item, endPosition);
            case MAP:
                return decodeMap(bb, item, endPosition);
        }
        throw Sugar.makeThrow("Unable to parse object");
    }

    private void decodeHeader() {
        ByteBuilder bb = new ByteBuilder(headerCache).resetPosition();
        int headLen = bb.resetPosition().readUint();
        if (bb.getReadableCount() == headLen) {
            int msgEndPosition = bb.readUint() + bb.getPosition();
            while (bb.getPosition() < msgEndPosition) {
                String key = Integer.toString(bb.readUint());
                int subSetEndPosition = bb.readUint() + bb.getPosition();
                TreeSet<Integer> subSet = new TreeSet<>();
                while (bb.getPosition() < subSetEndPosition) {
                    subSet.add(bb.readUint());
                }
                refFieldMap.put(key, subSet);
            }

            int fieldEndPosition = bb.readUint() + bb.getPosition();
            while (bb.getPosition() < fieldEndPosition) {
                Integer id = bb.readUint();
                SchemaItem item = new SchemaItem();
                item.setTypeCode(bb.readByte());
                int keyLen = bb.readUint();
                if (keyLen != 0) {
                    try {
                        item.setKey(new String(bb.readBytes(keyLen), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                int ref = bb.readSint();
                if (ref >= 0) {
                    item.setEmbeddedRef(Integer.toString(ref));
                }
                fieldItemMap.put(id, item);
            }
        } else {
            throw Sugar.makeThrow("Message header format error");
        }
    }

    private void link() {
        tmpRefMap.clear();
        fieldLink.clear();
        refLink.clear();
        linkType(adapterType, fieldItemMap.get(startSeq));
    }

    private void linkType(TypeNode typeNode, SchemaItem item) {
        Type type = typeNode.getType();
        if (Reflects.isList(type)) {
            if (item.getDecodeType().equals(SchemaItem.DecodeType.LIST)) {
                String ref = item.getEmbeddedRef();
                refLink.put(ref, typeNode.getRawType());
                Integer subFieldId = refFieldMap.get(ref).iterator().next();
                linkType(typeNode.getArguments(0), fieldItemMap.get(subFieldId));
            } else {
                throw Sugar.makeThrow("header is inconsistent with type");
            }
        } else if (Reflects.isArray(type)) {
            if (item.getDecodeType().equals(SchemaItem.DecodeType.ARRAY)) {
                String ref = item.getEmbeddedRef();
                refLink.put(ref, typeNode.getRawType());
                Integer subFieldId = refFieldMap.get(ref).iterator().next();
                linkType(typeNode.getArguments(0), fieldItemMap.get(subFieldId));
            } else {
                throw Sugar.makeThrow("header is inconsistent with type");
            }
        } else if (Reflects.isMap(type)) {
            if (item.getDecodeType().equals(SchemaItem.DecodeType.MAP)) {
                String ref = item.getEmbeddedRef();
                refLink.put(ref, typeNode.getRawType());

                Iterator<Integer> iterator = refFieldMap.get(ref).iterator();
                Integer keyFieldId = iterator.next();
                linkType(typeNode.getArguments(0), fieldItemMap.get(keyFieldId));
                Integer valueFieldId = iterator.next();
                linkType(typeNode.getArguments(1), fieldItemMap.get(valueFieldId));
            } else {
                throw Sugar.makeThrow("header is inconsistent with type");
            }
        } else if (Reflects.isClass(type) && Sugar.checkInherit((Class) type, SerializableMessage.class)) {
            if (item.getDecodeType().equals(SchemaItem.DecodeType.EMBEDDED)) {
                Class msgClass = (Class) type;
                if (!tmpRefMap.containsKey(msgClass.getName())) {
                    String ref = item.getEmbeddedRef();
                    refLink.put(ref, msgClass);
                    tmpRefMap.put(msgClass.getName(), ref);

                    for (Integer subFieldId : refFieldMap.get(ref)) {
                        SchemaItem subItem = fieldItemMap.get(subFieldId);
                        try {
                            Field f = msgClass.getDeclaredField(subItem.getKey());
                            if (f != null) {
                                f.setAccessible(true);
                                fieldLink.put(subFieldId, f);
                                linkType(new TypeNode(f.getGenericType()), subItem);
                            }
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                throw Sugar.makeThrow("header is inconsistent with type");
            }
        }
    }

    private ByteBuilder encodeMessage(Object msg, SchemaItem item, Integer fieldId, boolean needHeader) {
        ByteBuilder bb = new ByteBuilder();
        if (msg == null) {
            return bb;
        }
        // TODO: 空处理
        if (needHeader) {
            bb.putUint(fieldId << 3 | item.getEncodeType().getTypeNum() & 0x07);
        }
        switch (item.getDecodeType()) {
            case BOOLEAN:
                bb.putBoolean((boolean) msg);
                break;
            case BYTE:
                bb.putByte((byte) msg);
                break;
            case INT16:
                bb.putShort((short) msg);
                break;
            case UINT16:
                bb.putShort((short) msg);
                break;
            case CHAR:
                bb.putChar((char) msg);
                break;
            case INT32:
                bb.putInt((int) msg);
                break;
            case UINT32:
                bb.putUint((int) msg);
                break;
            case SINT32:
                bb.putSint((int) msg);
                break;
            case INT64:
                bb.putLong((long) msg);
                break;
            case UINT64:
                bb.putUlong((long) msg);
                break;
            case SINT64:
                bb.putSlong((long) msg);
                break;
            case FLOAT:
                bb.putFloat((float) msg);
                break;
            case DOUBLE:
                bb.putDouble((double) msg);
                break;
            case STRING:
                encodeString(bb, msg);
                break;
            case EMBEDDED:
                encodeEmbedded(bb, msg, item);
                break;
            case LIST:
                encodeList(bb, msg, item);
                break;
            case ARRAY:
                encodeArray(bb, msg, item);
                break;
            case MAP:
                encodeMap(bb, msg, item);
                break;
        }
        return bb;
    }

    private void encodeString(ByteBuilder bb, Object msg) {
        try {
            byte[] bytes = ((String) msg).getBytes("UTF-8");
            bb.putUint(bytes.length).putBytes(bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void encodeEmbedded(ByteBuilder bb, Object msg, SchemaItem item) {
        TreeSet<Integer> embeddedFieldIds = refFieldMap.get(item.getEmbeddedRef());
        ByteBuilder embeddedBB = new ByteBuilder();
        try {
            for (int i : embeddedFieldIds) {
                Object o = fieldLink.get(i).get(msg);
                if (o != null) {
                    embeddedBB.putBytes(encodeMessage(o, fieldItemMap.get(i), i, true));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        bb.putUint(embeddedBB.getLimit()).putBytes(embeddedBB);
    }

    private void encodeList(ByteBuilder bb, Object msg, SchemaItem item) {
        Integer listSubFieldId = refFieldMap.get(item.getEmbeddedRef()).iterator().next();
        SchemaItem listSubItem = fieldItemMap.get(listSubFieldId);
        ByteBuilder listBB = new ByteBuilder();
        List list = (List) msg;

        boolean subItemNeedHeader = listSubItem.getEncodeType().equals(SchemaItem.EncodeType.LENGTH_DELIMITED);
        for (Object aList : list) {
            listBB.putBytes(encodeMessage(aList, listSubItem, listSubFieldId, subItemNeedHeader));
        }
        bb.putUint(listBB.getLimit()).putBytes(listBB);
    }

    private void encodeArray(ByteBuilder bb, Object msg, SchemaItem item) {
        Integer arraySubFieldId = refFieldMap.get(item.getEmbeddedRef()).iterator().next();
        SchemaItem arraySubItem = fieldItemMap.get(arraySubFieldId);
        ByteBuilder arrayBB = new ByteBuilder();

        if (msg instanceof Object[]) {
            Object[] array = (Object[]) msg;
            for (Object anArray : array)
                arrayBB.putBytes(encodeMessage(anArray, arraySubItem, arraySubFieldId, true));
        } else {
            switch (arraySubItem.getDecodeType()) {

                case BOOLEAN:
                    boolean[] booleans = (boolean[]) msg;
                    for (boolean i : booleans)
                        arrayBB.putBoolean(i);
                    break;
                case BYTE:
                    byte[] bytes = (byte[]) msg;
                    for (byte i : bytes)
                        arrayBB.putByte(i);
                    break;
                case INT16:
                case UINT16:
                    short[] shorts = (short[]) msg;
                    for (short i : shorts)
                        arrayBB.putShort(i);
                    break;
                case CHAR:
                    char[] chars = (char[]) msg;
                    for (char i : chars)
                        arrayBB.putChar(i);
                    break;
                case INT32:
                    int[] ints = (int[]) msg;
                    for (int i : ints)
                        arrayBB.putInt(i);
                    break;
                case UINT32:
                    int[] uints = (int[]) msg;
                    for (int i : uints)
                        arrayBB.putInt(i);
                    break;
                case SINT32:
                    int[] sints = (int[]) msg;
                    for (int i : sints)
                        arrayBB.putInt(i);
                    break;
                case INT64:
                    long[] longs = (long[]) msg;
                    for (long i : longs)
                        arrayBB.putLong(i);
                    break;
                case UINT64:
                    long[] ulongs = (long[]) msg;
                    for (long i : ulongs)
                        arrayBB.putUlong(i);
                    break;
                case SINT64:
                    long[] slongs = (long[]) msg;
                    for (long i : slongs)
                        arrayBB.putSlong(i);
                    break;
                case FLOAT:
                    float[] floats = (float[]) msg;
                    for (float i : floats)
                        arrayBB.putFloat(i);
                    break;
                case DOUBLE:
                    double[] doubles = (double[]) msg;
                    for (double i : doubles)
                        arrayBB.putDouble(i);
                    break;
            }
        }
        bb.putUint(arrayBB.getLimit()).putBytes(arrayBB);
    }

    private void encodeMap(ByteBuilder bb, Object msg, SchemaItem item) {
        TreeSet<Integer> mapFieldIds = refFieldMap.get(item.getEmbeddedRef());
        Iterator<Integer> iter = mapFieldIds.iterator();
        Integer keyFieldId = iter.next();
        SchemaItem keyItem = fieldItemMap.get(keyFieldId);
        Integer valueFieldId = iter.next();
        SchemaItem valueItem = fieldItemMap.get(valueFieldId);

        ByteBuilder mapBB = new ByteBuilder();
        Map map = (Map) msg;
        for (Object key : map.keySet()) {
            mapBB.putBytes(encodeMessage(key, keyItem, keyFieldId, true));
            mapBB.putBytes(encodeMessage(map.get(key), valueItem, valueFieldId, true));
        }
        bb.putUint(mapBB.getLimit()).putBytes(mapBB);
    }

    private void encodeHeader() {
        ByteBuilder msgbb = new ByteBuilder();
        for (Map.Entry<String, TreeSet<Integer>> entry : refFieldMap.entrySet()) {
            msgbb.putUint(Integer.parseInt(entry.getKey()));   // key
            ByteBuilder msgValueSet = new ByteBuilder();
            for (int i : entry.getValue()) {
                msgValueSet.putUint(i);
            }
            msgbb.putUint(msgValueSet.getLimit()).putBytes(msgValueSet);  // value length + value
        }
        ByteBuilder msgbbWithLen = new ByteBuilder().putUint(msgbb.getLimit()).putBytes(msgbb.toBytes());

        ByteBuilder fieldbb = new ByteBuilder();
        for (Map.Entry<Integer, SchemaItem> entry : fieldItemMap.entrySet()) {
            fieldbb.putUint(entry.getKey()).putByte(entry.getValue().getTypeCode());
            try {
                if (Strings.isBlank(entry.getValue().getKey())) {
                    fieldbb.putByte((byte) 0x00);
                } else {
                    byte[] fieldNameBytes = entry.getValue().getKey().getBytes("UTF-8");
                    fieldbb.putUint(fieldNameBytes.length).putBytes(fieldNameBytes);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (Strings.isBlank(entry.getValue().getEmbeddedRef())) {
                fieldbb.putSint(-1);
            } else {
                fieldbb.putSint(Integer.parseInt(entry.getValue().getEmbeddedRef()));
            }
        }
        ByteBuilder fieldbbWithLen = new ByteBuilder().putUint(fieldbb.getLimit()).putBytes(fieldbb.toBytes());
        ByteBuilder total = new ByteBuilder().putUint(msgbbWithLen.getLimit() + fieldbbWithLen.getLimit()).putBytes(msgbbWithLen.toBytes()).putBytes(fieldbbWithLen.toBytes());
        this.headerCache = total.toBytes();
        this.headerHash = Arrays.hashCode(this.headerCache);
    }

    private Integer create(TypeNode typeNode, String fieldKey, String own) {
        Type type = typeNode.getType();
        Integer returnFieldId;
        if (Reflects.isBoolean(type)) {
            returnFieldId = addSchemaItem(new SchemaItem(fieldKey, SchemaItem.DecodeType.BOOLEAN, SchemaItem.EncodeType.VARINT, null), own);
        } else if (Reflects.isByte(type)) {
            returnFieldId = addSchemaItem(new SchemaItem(fieldKey, SchemaItem.DecodeType.BYTE, SchemaItem.EncodeType.BIT8, null), own);
        } else if (Reflects.isChar(type)) {
            returnFieldId = addSchemaItem(new SchemaItem(fieldKey, SchemaItem.DecodeType.CHAR, SchemaItem.EncodeType.BIT16, null), own);
        } else if (Reflects.isShort(type)) {
            returnFieldId = addSchemaItem(new SchemaItem(fieldKey, SchemaItem.DecodeType.INT16, SchemaItem.EncodeType.BIT16, null), own);
        } else if (Reflects.isInt(type)) {
            returnFieldId = addSchemaItem(new SchemaItem(fieldKey, SchemaItem.DecodeType.SINT32, SchemaItem.EncodeType.VARINT, null), own);
        } else if (Reflects.isLong(type)) {
            returnFieldId = addSchemaItem(new SchemaItem(fieldKey, SchemaItem.DecodeType.SINT64, SchemaItem.EncodeType.VARINT, null), own);
        } else if (Reflects.isFloat(type)) {
            returnFieldId = addSchemaItem(new SchemaItem(fieldKey, SchemaItem.DecodeType.FLOAT, SchemaItem.EncodeType.BIT32, null), own);
        } else if (Reflects.isDouble(type)) {
            returnFieldId = addSchemaItem(new SchemaItem(fieldKey, SchemaItem.DecodeType.DOUBLE, SchemaItem.EncodeType.BIT64, null), own);
        } else if (Reflects.isString(type)) {
            returnFieldId = addSchemaItem(new SchemaItem(fieldKey, SchemaItem.DecodeType.STRING, SchemaItem.EncodeType.LENGTH_DELIMITED, null), own);
        } else if (Reflects.isArray(type)) {
            String ref = generateRef();
            SchemaItem schemaItem = new SchemaItem(fieldKey, SchemaItem.DecodeType.ARRAY, SchemaItem.EncodeType.LENGTH_DELIMITED, ref);
            returnFieldId = addSchemaItem(schemaItem, own);
            addLinkField(ref, typeNode);
            create(typeNode.getArguments(0), null, ref);
        } else if (Reflects.isList(type)) {
            String ref = generateRef();
            SchemaItem schemaItem = new SchemaItem(fieldKey, SchemaItem.DecodeType.LIST, SchemaItem.EncodeType.LENGTH_DELIMITED, ref);
            returnFieldId = addSchemaItem(schemaItem, own);
            addLinkField(ref, typeNode);
            create(typeNode.getArguments(0), null, ref);
        } else if (Reflects.isMap(type)) {
            String ref = generateRef();
            SchemaItem schemaItem = new SchemaItem(fieldKey, SchemaItem.DecodeType.MAP, SchemaItem.EncodeType.LENGTH_DELIMITED, ref);
            returnFieldId = addSchemaItem(schemaItem, own);
            addLinkField(ref, typeNode);
            create(typeNode.getArguments(0), null, ref);
            create(typeNode.getArguments(1), null, ref);
        } else if (Reflects.isClass(type) && Sugar.checkInherit((Class) type, SerializableMessage.class)) {
            Class rawType = (Class) type;
            String className = rawType.getName();
            String ref;
            boolean handled = false;
            if (tmpRefMap.containsKey(className)) {
                ref = tmpRefMap.get(className);
                handled = true;
            } else {
                ref = generateRef();
                tmpRefMap.put(className, ref);
                addLinkField(ref, typeNode);
            }
            SchemaItem schemaItem = new SchemaItem(fieldKey, SchemaItem.DecodeType.EMBEDDED, SchemaItem.EncodeType.LENGTH_DELIMITED, ref);
            returnFieldId = addSchemaItem(schemaItem, own);

            if (!handled) {
                Field[] fields = rawType.getDeclaredFields();
                for (Field f : fields) {
                    if (f.getAnnotation(IgnoreField.class) == null) {
                        String fieldName = f.getName();
                        Integer id = create(new TypeNode(f.getGenericType()), fieldName, ref);
                        if (id != null) {
                            f.setAccessible(true);
                            fieldLink.put(id, f);
                        }
                    }
                }
            }
        } else {
            throw Sugar.makeThrow("Unable to parse type %s", type.getTypeName());
        }
        return returnFieldId;
    }

    private void addLinkField(String ref, TypeNode typeNode) {
        if (!refLink.containsKey(ref)) {
            refLink.put(ref, typeNode.getRawType());
        }
    }

    private String generateRef() {
        return Integer.toString(refSeq.getAndAdd(1));
    }

    private Integer addSchemaItem(SchemaItem schemaItem, String own) {
        Integer fieldNum = seq.getAndAdd(1);
        fieldItemMap.put(fieldNum, schemaItem);
        addRelation(own, fieldNum);
        return fieldNum;
    }

    //    @Override
    //    public String toString() {
    //        StringBuilder sb = new StringBuilder();
    //        sb.append("===fieldItemMap===\n").append(Strings.mapToString(fieldItemMap, 5));
    //        sb.append("===fieldLink===\n").append(Strings.mapToString(fieldLink, 5));
    //        sb.append("===refFieldMap===\n").append(Strings.mapToString(refFieldMap, 5));
    //        sb.append("===refLink===\n").append(Strings.mapToString(refLink, 5));
    //        sb.append("===tmpRefMap===\n").append(Strings.mapToString(tmpRefMap, 50));
    //        sb.append(String.format("\nHeader[%d] : ", headerCache.length)).append(Strings.bytesToHexString(headerCache)).append("\n");
    //        sb.append(String.format("HeaderHash : 0x%s", Integer.toHexString(headerHash).toUpperCase()));
    //        return sb.toString();
    //    }

    private void addRelation(String own, Integer fieldId) {
        if (refFieldMap.containsKey(own)) {
            refFieldMap.get(own).add(fieldId);
        } else {
            refFieldMap.put(own, new TreeSet<Integer>() {{
                add(fieldId);
            }});
        }
    }

}
