package cn.autolabor.util.autobuf;

import cn.autolabor.message.navigation.Msg2DOdometry;
import cn.autolabor.message.navigation.Msg2DPose;
import cn.autolabor.message.navigation.Msg2DTwist;
import cn.autolabor.util.Strings;
import cn.autolabor.util.Sugar;
import cn.autolabor.util.collections.Pair;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoBufEncoder {

    private AtomicInteger seq = new AtomicInteger(1);

    private String rootName = "@ROOT";

    private Map<String, Integer> fieldIdMap = new LinkedHashMap<>(); // URI -- ID
    private Map<String, SchemaItem> fieldItemMap = new LinkedHashMap<>(); // URI -- ITEM
    private Map<String, TreeSet<String>> refFieldMap = new LinkedHashMap<>(); // REF -- URIS

    private ByteBuilder header;
    private ByteBuilder body;


    private AutoBufEncoder() {
    }

    public static AutoBufEncoder toEncoder(AutoBufObject object) {
        AutoBufEncoder encoder = new AutoBufEncoder();
        encoder.encode(object);
        return encoder;
    }

    public static byte[] toBytes(AutoBufObject object) {
        AutoBufEncoder encoder = new AutoBufEncoder();
        encoder.encode(object);
        return encoder.getMessageBytes();
    }

    public static void main(String[] args) {
        Msg2DOdometry odom = new Msg2DOdometry();
        odom.setPose(new Msg2DPose(1, 2, 3));
        odom.setTwist(new Msg2DTwist(4, 5, 6));

        AutoBufObject object = AutoBufBuilder.createEmbedded(null, "request").putRawData("param1", new Msg2DPose(1, 2, 3)).putRawData("param2", new Msg2DTwist(4, 5, 6)).putRawData("param3", odom);
        AutoBufEncoder encoder = AutoBufEncoder.toEncoder(object);
        System.out.println(Strings.bytesToHexString(encoder.getHeader()));
        System.out.println(Strings.bytesToHexString(encoder.getBody()));
        System.out.println(Strings.bytesToHexString(encoder.getMessageBytes()));
        System.out.println(encoder.getMessageBytes().length);

    }

    private void encode(AutoBufObject object) {
        object.setTmpUri(createUri(object, object.getKey(), rootName));
        this.body = encodeMessage(object, rootName, true);
        //        System.out.println("====fieldItemMap====");
        //        System.out.println(Strings.mapToString(fieldItemMap));
        //        System.out.println("====refFieldMap====");
        //        System.out.println(Strings.mapToString(refFieldMap));
        this.header = encodeHeader();
    }

    private ByteBuilder encodeMessage(AutoBufObject object, String upperUri, boolean needHeader) {
        ByteBuilder bb = new ByteBuilder();
        SchemaItem.DecodeType type = object.getType();
        Pair<Integer, SchemaItem> pair = createAndGetItem(object, upperUri);
        if (needHeader) {
            bb.putUint(pair.getKey() << 3 | pair.getValue().getEncodeType().getTypeNum() & 0x07);
        }

        switch (type) {

            case BOOLEAN:
                bb.putBoolean((boolean) object.getData());
                break;
            case BYTE:
                bb.putByte((byte) object.getData());
                break;
            case INT16:
                bb.putShort((short) object.getData());
                break;
            case UINT16:
                bb.putShort((short) object.getData());
                break;
            case CHAR:
                bb.putChar((char) object.getData());
                break;
            case INT32:
                bb.putInt((int) object.getData());
                break;
            case UINT32:
                bb.putUint((int) object.getData());
                break;
            case SINT32:
                bb.putSint((int) object.getData());
                break;
            case INT64:
                bb.putLong((long) object.getData());
                break;
            case UINT64:
                bb.putUlong((long) object.getData());
                break;
            case SINT64:
                bb.putSlong((long) object.getData());
                break;
            case FLOAT:
                bb.putFloat((float) object.getData());
                break;
            case DOUBLE:
                bb.putDouble((double) object.getData());
                break;
            case STRING:
                encodeString(bb, object);
                break;
            case LIST:
                encodeList(bb, (AutoBufList) object);
                break;
            case ARRAY:
                encodeArray(bb, (AutoBufArray) object);
                break;
            case EMBEDDED:
                encodeEmbedded(bb, (AutoBufEmbedded) object);
                break;
            case MAP:
                encodeMap(bb, (AutoBufMap) object);
                break;
        }
        return bb;
    }

    /***
     *  编码格式
     *  TOTAL :| HEAD_LENGTH(UINT32) | MESSAGES_LENGTH(UINT32) | MESSAGE_CONTENT | FIELD_LENGTH(UINT32) | FIELD_CONTENT |
     *  MESSAGE_CONTENT : | REF1(SINT32) | FIELD_ID_LIST1 | REF2(SINT32) | FIELD_ID_LIST2 | ...
     *  FIELD_ID_LIST : | LENGTH(UINT32) | FIELD_ID1(UINT32) | FIELD_ID2(UINT32) | ...
     *  FIELD_CONTENT : | ID(UINT32) | TYPE_CODE(BYTE) | FIELD_NAME_LENGTH(UINT32) | FIELD_NAME(STRING) | REF(SINT32) (-1 -> null) | ...
     */
    private ByteBuilder encodeHeader() {
        Map<String, Integer> castMap = getCastMap();
        ByteBuilder msgbb = new ByteBuilder();
        for (Map.Entry<String, TreeSet<String>> entry : refFieldMap.entrySet()) {
            msgbb.putUint(castMap.get(entry.getKey()));   // key
            ByteBuilder msgValueSet = new ByteBuilder();
            Set<Integer> indexs = uriToIndex(entry.getValue());
            for (Integer i : indexs) {
                msgValueSet.putUint(i);
            }
            msgbb.putUint(msgValueSet.getLimit()).putBytes(msgValueSet);  // value length + value
        }
        ByteBuilder msgbbWithLen = new ByteBuilder().putUint(msgbb.getLimit()).putBytes(msgbb.toBytes());

        ByteBuilder fieldbb = new ByteBuilder();
        for (Map.Entry<String, SchemaItem> entry : fieldItemMap.entrySet()) {
            fieldbb.putUint(fieldIdMap.get(entry.getKey())).putByte(entry.getValue().getTypeCode());
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
                fieldbb.putSint(castMap.get(entry.getValue().getEmbeddedRef()));
            }
        }
        ByteBuilder fieldbbWithLen = new ByteBuilder().putUint(fieldbb.getLimit()).putBytes(fieldbb.toBytes());
        ByteBuilder total = new ByteBuilder().putUint(msgbbWithLen.getLimit() + fieldbbWithLen.getLimit()).putBytes(msgbbWithLen.toBytes()).putBytes(fieldbbWithLen.toBytes());
        return total;
    }

    private Set<Integer> uriToIndex(Set<String> uris) {
        Set<Integer> indexs = new TreeSet<>();
        for (String uri : uris) {
            indexs.add(fieldIdMap.get(uri));
        }
        return indexs;
    }

    private void encodeString(ByteBuilder bb, AutoBufObject object) {
        try {
            byte[] bytes = ((String) object.getData()).getBytes("UTF-8");
            bb.putUint(bytes.length).putBytes(bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void encodeList(ByteBuilder bb, AutoBufList autoBufList) {
        ArrayList<AutoBufObject> rawList = autoBufList.getData();
        boolean subNeedHeader = needHeader(autoBufList.getSubItemType());
        ByteBuilder listBB = new ByteBuilder();
        for (int i = 0; i < rawList.size(); i++) {
            AutoBufObject subObject = rawList.get(i);
            subObject.setTmpUri(createUri(subObject, null, autoBufList.getTmpUri()));
            listBB.putBytes(encodeMessage(subObject, autoBufList.getTmpUri(), subNeedHeader));
        }
        bb.putUint(listBB.getLimit()).putBytes(listBB);
    }

    private void encodeArray(ByteBuilder bb, AutoBufArray autoBufArray) {
        SchemaItem.DecodeType subDecodeType = autoBufArray.getSubItemType();
        ByteBuilder arrayBB = new ByteBuilder();
        switch (subDecodeType) {

            case BOOLEAN:
                checkAndAddItem(subDecodeType, autoBufArray.getTmpUri());
                boolean[] booleans = (boolean[]) autoBufArray.getData();
                for (boolean i : booleans)
                    arrayBB.putBoolean(i);
                break;
            case BYTE:
                checkAndAddItem(subDecodeType, autoBufArray.getTmpUri());
                byte[] bytes = (byte[]) autoBufArray.getData();
                for (byte i : bytes)
                    arrayBB.putByte(i);
                break;
            case INT16:
            case UINT16:
                checkAndAddItem(subDecodeType, autoBufArray.getTmpUri());
                short[] shorts = (short[]) autoBufArray.getData();
                for (short i : shorts)
                    arrayBB.putShort(i);
                break;
            case CHAR:
                checkAndAddItem(subDecodeType, autoBufArray.getTmpUri());
                char[] chars = (char[]) autoBufArray.getData();
                for (char i : chars)
                    arrayBB.putChar(i);
                break;
            case INT32:
                checkAndAddItem(subDecodeType, autoBufArray.getTmpUri());
                int[] ints = (int[]) autoBufArray.getData();
                for (int i : ints)
                    arrayBB.putInt(i);
                break;
            case UINT32:
                checkAndAddItem(subDecodeType, autoBufArray.getTmpUri());
                int[] uints = (int[]) autoBufArray.getData();
                for (int i : uints)
                    arrayBB.putInt(i);
                break;
            case SINT32:
                checkAndAddItem(subDecodeType, autoBufArray.getTmpUri());
                int[] sints = (int[]) autoBufArray.getData();
                for (int i : sints)
                    arrayBB.putInt(i);
                break;
            case INT64:
                checkAndAddItem(subDecodeType, autoBufArray.getTmpUri());
                long[] longs = (long[]) autoBufArray.getData();
                for (long i : longs)
                    arrayBB.putLong(i);
                break;
            case UINT64:
                checkAndAddItem(subDecodeType, autoBufArray.getTmpUri());
                long[] ulongs = (long[]) autoBufArray.getData();
                for (long i : ulongs)
                    arrayBB.putUlong(i);
                break;
            case SINT64:
                checkAndAddItem(subDecodeType, autoBufArray.getTmpUri());
                long[] slongs = (long[]) autoBufArray.getData();
                for (long i : slongs)
                    arrayBB.putSlong(i);
                break;
            case FLOAT:
                checkAndAddItem(subDecodeType, autoBufArray.getTmpUri());
                float[] floats = (float[]) autoBufArray.getData();
                for (float i : floats)
                    arrayBB.putFloat(i);
                break;
            case DOUBLE:
                checkAndAddItem(subDecodeType, autoBufArray.getTmpUri());
                double[] doubles = (double[]) autoBufArray.getData();
                for (double i : doubles)
                    arrayBB.putDouble(i);
                break;
            case STRING:
            case LIST:
            case ARRAY:
            case EMBEDDED:
            case MAP:
                ArrayList<AutoBufObject> rawList = (ArrayList<AutoBufObject>) autoBufArray.getData();
                for (int i = 0; i < rawList.size(); i++) {
                    AutoBufObject subObject = rawList.get(i);
                    subObject.setTmpUri(createUri(subObject, null, autoBufArray.getTmpUri()));
                    arrayBB.putBytes(encodeMessage(subObject, autoBufArray.getTmpUri(), true));
                }
                break;
            default:
                throw Sugar.makeThrow("Unable to encode data of type %s", subDecodeType);

        }
        bb.putUint(arrayBB.getLimit()).putBytes(arrayBB);

    }

    private void encodeEmbedded(ByteBuilder bb, AutoBufEmbedded autoBufEmbedded) {
        HashMap<String, AutoBufObject> rawMap = (HashMap<String, AutoBufObject>) autoBufEmbedded.getData();
        ByteBuilder embeddedBB = new ByteBuilder();
        for (Map.Entry<String, AutoBufObject> subMap : rawMap.entrySet()) {
            AutoBufObject subObject = subMap.getValue();
            subObject.setTmpUri(createUri(subObject, subMap.getKey(), autoBufEmbedded.getId()));
            embeddedBB.putBytes(encodeMessage(subObject, autoBufEmbedded.getId(), true));
        }
        bb.putUint(embeddedBB.getLimit()).putBytes(embeddedBB);
    }

    private void encodeMap(ByteBuilder bb, AutoBufMap autoBufMap) {
        HashMap<AutoBufObject, AutoBufObject> rawMap = (HashMap<AutoBufObject, AutoBufObject>) autoBufMap.getData();
        ByteBuilder mapBB = new ByteBuilder();
        for (Map.Entry<AutoBufObject, AutoBufObject> subMap : rawMap.entrySet()) {
            AutoBufObject keyObject = subMap.getKey();
            keyObject.setTmpUri(createUri(keyObject, "key", autoBufMap.getTmpUri()));
            mapBB.putBytes(encodeMessage(keyObject, autoBufMap.getTmpUri(), true));

            AutoBufObject valueObject = subMap.getValue();
            valueObject.setTmpUri(createUri(valueObject, "value", autoBufMap.getTmpUri()));
            mapBB.putBytes(encodeMessage(valueObject, autoBufMap.getTmpUri(), true));
        }
        bb.putUint(mapBB.getLimit()).putBytes(mapBB);
    }

    private Map<String, Integer> getCastMap() {
        int i = 0;
        Set<String> keys = new LinkedHashSet<>(refFieldMap.keySet());
        Map<String, Integer> castMap = new LinkedHashMap<>();
        keys.remove(rootName);
        castMap.put(rootName, i++);
        for (String key : keys) {
            castMap.put(key, i++);
        }
        return castMap;
    }

    private void checkAndAddItem(SchemaItem.DecodeType type, String upperUri) {
        String curUri = String.format("%s::%s", upperUri, type.name());
        if (!fieldIdMap.containsKey(curUri)) {
            Integer fieldId = seq.getAndAdd(1);
            fieldIdMap.put(curUri, fieldId);
            SchemaItem item = new SchemaItem(null, type, SchemaItem.EncodeType.getFromDecodeType(type), null);
            fieldItemMap.put(curUri, item);

            refFieldMap.put(upperUri, new TreeSet<String>() {{
                add(curUri);
            }});
        }
    }

    private Pair<Integer, SchemaItem> createAndGetItem(AutoBufObject object, String upperUri) {
        String uri = object.getTmpUri();
        if (fieldIdMap.containsKey(uri)) {
            return new Pair<>(fieldIdMap.get(uri), fieldItemMap.get(uri));
        } else {
            Integer fieldId = seq.getAndAdd(1);
            fieldIdMap.put(uri, fieldId);
            SchemaItem item = null;
            SchemaItem.DecodeType type = object.getType();
            if (type == SchemaItem.DecodeType.EMBEDDED) {
                item = new SchemaItem(object.getKey(), SchemaItem.DecodeType.EMBEDDED, SchemaItem.EncodeType.LENGTH_DELIMITED, ((AutoBufEmbedded) object).getId());
            } else if (type == SchemaItem.DecodeType.LIST || type == SchemaItem.DecodeType.ARRAY || type == SchemaItem.DecodeType.MAP) {
                item = new SchemaItem(object.getKey(), type, SchemaItem.EncodeType.getFromDecodeType(type), uri);
            } else {
                item = new SchemaItem(object.getKey(), type, SchemaItem.EncodeType.getFromDecodeType(type), null);
            }
            fieldItemMap.put(uri, item);
            if (refFieldMap.containsKey(upperUri)) {
                refFieldMap.get(upperUri).add(uri);
            } else {
                refFieldMap.put(upperUri, new TreeSet<String>() {{
                    add(uri);
                }});
            }
            return new Pair<>(fieldId, item);
        }
    }

    private String createUri(AutoBufObject object, String key, String upperUri) {
        if (Strings.isNotBlank(key))
            return String.format("%s::%s[%s]", upperUri, object.getType().name(), key);
        else
            return String.format("%s::%s", upperUri, object.getType().name());
    }

    /***
     *  判断是否需要加头信息
     * @param type
     * @return
     */
    private boolean needHeader(SchemaItem.DecodeType type) {
        return type == SchemaItem.DecodeType.STRING || type == SchemaItem.DecodeType.LIST
                || type == SchemaItem.DecodeType.ARRAY || type == SchemaItem.DecodeType.MAP
                || type == SchemaItem.DecodeType.EMBEDDED;
    }

    /***
     *  判断是否需要特殊处理
     * @param type
     * @return
     */
    private boolean isComplexType(SchemaItem.DecodeType type) {
        return type == SchemaItem.DecodeType.LIST
                || type == SchemaItem.DecodeType.ARRAY
                || type == SchemaItem.DecodeType.MAP
                || type == SchemaItem.DecodeType.EMBEDDED;
    }

    public byte[] getHeader() {
        return header.toBytes();
    }

    public byte[] getBody() {
        return body.toBytes();
    }

    public byte[] getMessageBytes() {
        return header.putBytes(body).toBytes();
    }

    @Override
    public String toString() {
        return "AutoBufEncoder{" +
                "seq=" + seq +
                ",\n fieldIdMap=" + fieldIdMap +
                ",\n fieldItemMap=" + fieldItemMap +
                ",\n refFieldMap=" + refFieldMap +
                '}';
    }
}
