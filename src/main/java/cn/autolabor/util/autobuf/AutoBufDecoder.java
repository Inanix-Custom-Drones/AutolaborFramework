package cn.autolabor.util.autobuf;

import cn.autolabor.util.Strings;
import cn.autolabor.util.Sugar;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class AutoBufDecoder {

    private LinkedHashMap<Integer, SchemaItem> fieldItemMap = new LinkedHashMap<>(); // ID -- ITEM
    private LinkedHashMap<String, TreeSet<Integer>> refFieldMap = new LinkedHashMap<>(); // REF -- ID
    private byte[] header;
    private byte[] body;

    private AutoBufDecoder() {
    }

    public static AutoBufObject toObject(byte[] bytes) {
        AutoBufDecoder decoder = new AutoBufDecoder();
        decoder.segmentation(bytes);
        decoder.decodeHeader();

        //        System.out.println("========");
        //        System.out.println(Strings.mapToString(decoder.fieldItemMap));
        //        System.out.println("========");
        //        System.out.println(Strings.mapToString(decoder.refFieldMap));

        ByteBuilder bb = new ByteBuilder(decoder.body).resetPosition();
        SchemaItem item = decoder.fieldItemMap.get((bb.readUint() >>> 3));
        int endPosition = 0;
        if (item.getEncodeType().equals(SchemaItem.EncodeType.LENGTH_DELIMITED)) {
            endPosition = bb.readUint() + bb.getPosition();
        }
        return decoder.decodeMessage(bb, item, endPosition);
    }

    public static AutoBufObject toObject(ByteBuilder byteBuilder) {
        AutoBufDecoder decoder = new AutoBufDecoder();
        decoder.segmentation(byteBuilder);
        decoder.decodeHeader();

        ByteBuilder bb = new ByteBuilder(decoder.body).resetPosition();
        SchemaItem item = decoder.fieldItemMap.get((bb.readUint() >>> 3));
        int endPosition = 0;
        if (item.getEncodeType().equals(SchemaItem.EncodeType.LENGTH_DELIMITED)) {
            endPosition = bb.readUint() + bb.getPosition();
        }
        return decoder.decodeMessage(bb, item, endPosition);
    }

    public static void main(String[] args) {
        AutoBufObject object = AutoBufBuilder.createEmbedded("hello", "ROOT").putRawData("param1", 12).putRawData("param2", "123")
                .putRawData("arraydata", new int[]{1, 2, 3});
        AutoBufEncoder encoder = AutoBufEncoder.toEncoder(object);

        System.out.println(Strings.bytesToHexString(encoder.getMessageBytes()));

        AutoBufDecoder decoder = new AutoBufDecoder();
        AutoBufObject parsed = decoder.toObject(encoder.getMessageBytes());
        System.out.println(parsed.toString());

    }

    private void segmentation(ByteBuilder bb) {
        int mark = bb.getPosition();
        int headerEndposition = bb.readUint() + bb.getPosition();
        header = bb.setPosition(mark).readBytes(headerEndposition - mark);
        body = bb.readBytes(bb.getReadableCount());
    }

    private void segmentation(byte[] bytes) {
        ByteBuilder bb = new ByteBuilder(bytes).resetPosition();
        segmentation(bb);
    }

    private void decodeHeader() {
        ByteBuilder bb = new ByteBuilder(header).resetPosition();
        int headLen = bb.readUint();
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

    private AutoBufObject decodeMessage(ByteBuilder bb, SchemaItem item, Integer endPosition) {
        switch (item.getDecodeType()) {
            case BOOLEAN:
                return new AutoBufObject(item.getKey(), bb.readBoolean(), SchemaItem.DecodeType.BOOLEAN);
            case BYTE:
                return new AutoBufObject(item.getKey(), bb.readByte(), SchemaItem.DecodeType.BYTE);
            case INT16:
                return new AutoBufObject(item.getKey(), bb.readShort(), SchemaItem.DecodeType.INT16);
            case UINT16:
                return new AutoBufObject(item.getKey(), bb.readShort(), SchemaItem.DecodeType.UINT16);
            case CHAR:
                return new AutoBufObject(item.getKey(), bb.readChar(), SchemaItem.DecodeType.CHAR);
            case INT32:
                return new AutoBufObject(item.getKey(), bb.readInt(), SchemaItem.DecodeType.INT32);
            case UINT32:
                return new AutoBufObject(item.getKey(), bb.readUint(), SchemaItem.DecodeType.UINT32);
            case SINT32:
                return new AutoBufObject(item.getKey(), bb.readSint(), SchemaItem.DecodeType.SINT32);
            case INT64:
                return new AutoBufObject(item.getKey(), bb.readLong(), SchemaItem.DecodeType.INT64);
            case UINT64:
                return new AutoBufObject(item.getKey(), bb.readUlong(), SchemaItem.DecodeType.UINT64);
            case SINT64:
                return new AutoBufObject(item.getKey(), bb.readSlong(), SchemaItem.DecodeType.SINT64);
            case FLOAT:
                return new AutoBufObject(item.getKey(), bb.readFloat(), SchemaItem.DecodeType.FLOAT);
            case DOUBLE:
                return new AutoBufObject(item.getKey(), bb.readDouble(), SchemaItem.DecodeType.DOUBLE);
            case STRING:
                return decodeString(item.getKey(), bb, endPosition);
            case EMBEDDED:
                return decodeEmbedded(item.getKey(), bb, item, endPosition);
            case LIST:
                return decodeList(item.getKey(), bb, item, endPosition);
            case ARRAY:
                return decodeArray(item.getKey(), bb, item, endPosition);
            case MAP:
                return decodeMap(item.getKey(), bb, item, endPosition);
        }
        throw Sugar.makeThrow("Unable to parse object");
    }

    private AutoBufMap decodeMap(String key, ByteBuilder bb, SchemaItem item, Integer endPosition) {
        Iterator<Integer> iter = refFieldMap.get(item.getEmbeddedRef()).iterator();
        int keyFieldId = iter.next();
        SchemaItem keyItem = fieldItemMap.get(keyFieldId);
        int valueFieldId = iter.next();
        SchemaItem valueItem = fieldItemMap.get(valueFieldId);

        AutoBufObject keyObject, valueObject;
        AutoBufMap map = AutoBufBuilder.createMap(key, keyItem.getDecodeType(), valueItem.getDecodeType());
        while (bb.getPosition() < endPosition) {
            if (keyItem.getEncodeType().equals(SchemaItem.EncodeType.LENGTH_DELIMITED)) {
                bb.readUint();
                int keyEndPosition = bb.readUint() + bb.getPosition();
                keyObject = decodeMessage(bb, keyItem, keyEndPosition);
            } else {
                bb.readUint();
                keyObject = decodeMessage(bb, keyItem, 0);
            }

            if (valueItem.getEncodeType().equals(SchemaItem.EncodeType.LENGTH_DELIMITED)) {
                bb.readUint();
                int valueEndPosition = bb.readUint() + bb.getPosition();
                valueObject = decodeMessage(bb, valueItem, valueEndPosition);
            } else {
                bb.readUint();
                valueObject = decodeMessage(bb, valueItem, 0);
            }
            map.put(keyObject, valueObject);
        }
        return map;
    }

    private AutoBufArray decodeArray(String key, ByteBuilder bb, SchemaItem item, Integer endPosition) {
        int subListFieldId = refFieldMap.get(item.getEmbeddedRef()).iterator().next();
        SchemaItem subListItem = fieldItemMap.get(subListFieldId);
        SchemaItem.DecodeType subItemType = subListItem.getDecodeType();
        int arrayLength;
        switch (subItemType) {
            case BOOLEAN:
                arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 1);
                boolean[] booleans = new boolean[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    booleans[i] = bb.readBoolean();
                }
                return new AutoBufArray(key, booleans, subItemType);
            case BYTE:
                arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 1);
                byte[] bytes = new byte[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    bytes[i] = bb.readByte();
                }
                return new AutoBufArray(key, bytes, subItemType);
            case CHAR:
                arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 2);
                char[] chars = new char[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    chars[i] = bb.readChar();
                }
                return new AutoBufArray(key, chars, subItemType);
            case INT16:
            case UINT16:
                arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 2);
                short[] shorts = new short[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    shorts[i] = bb.readShort();
                }
                return new AutoBufArray(key, shorts, subItemType);
            case INT32:
                arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 4);
                int[] ints = new int[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    ints[i] = bb.readInt();
                }
                return new AutoBufArray(key, ints, subItemType);
            case UINT32:
                arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 0);
                int[] uints = new int[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    uints[i] = bb.readUint();
                }
                return new AutoBufArray(key, uints, subItemType);
            case SINT32:
                arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 0);
                int[] sints = new int[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    sints[i] = bb.readSint();
                }
                return new AutoBufArray(key, sints, subItemType);
            case INT64:
                arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 8);
                long[] longs = new long[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    longs[i] = bb.readLong();
                }
                return new AutoBufArray(key, longs, subItemType);
            case UINT64:
                arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 0);
                long[] ulongs = new long[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    ulongs[i] = bb.readUlong();
                }
                return new AutoBufArray(key, ulongs, subItemType);
            case SINT64:
                arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 0);
                long[] slongs = new long[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    slongs[i] = bb.readSlong();
                }
                return new AutoBufArray(key, slongs, subItemType);
            case FLOAT:
                arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 4);
                float[] floats = new float[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    floats[i] = bb.readFloat();
                }
                return new AutoBufArray(key, floats, subItemType);
            case DOUBLE:
                arrayLength = bb.getArrayLength(bb.getPosition(), endPosition, 8);
                double[] doubles = new double[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    doubles[i] = bb.readDouble();
                }
                return new AutoBufArray(key, doubles, subItemType);
            case STRING:
            case ARRAY:
            case LIST:
            case EMBEDDED:
            case MAP:
                ArrayList<AutoBufObject> array = new ArrayList<>();
                while (bb.getPosition() < endPosition) {
                    bb.readUint();
                    int subListEndPosition = bb.readUint() + bb.getPosition();
                    array.add(decodeMessage(bb, subListItem, subListEndPosition));
                }
                return new AutoBufArray(key, array, subItemType);
            default:
                throw Sugar.makeThrow("Unable to parse data of type %s", subItemType);
        }
    }

    private AutoBufList decodeList(String key, ByteBuilder bb, SchemaItem item, Integer endPosition) {
        int subListFieldId = refFieldMap.get(item.getEmbeddedRef()).iterator().next();
        SchemaItem subListItem = fieldItemMap.get(subListFieldId);
        AutoBufList list = AutoBufBuilder.createList(key, subListItem.getDecodeType());
        if (subListItem.getEncodeType().equals(SchemaItem.EncodeType.LENGTH_DELIMITED)) {
            while (bb.getPosition() < endPosition) {
                bb.readUint();
                int subListEndPosition = bb.readUint() + bb.getPosition();
                list.add(decodeMessage(bb, subListItem, subListEndPosition));
            }
        } else {
            while (bb.getPosition() < endPosition) {
                list.add(decodeMessage(bb, subListItem, 0));
            }
        }
        return list;
    }

    private AutoBufEmbedded decodeEmbedded(String key, ByteBuilder bb, SchemaItem item, Integer endPosition) {
        AutoBufEmbedded embedded = AutoBufBuilder.createEmbedded(key, item.getEmbeddedRef());
        while (bb.getPosition() < endPosition) {
            int header = bb.readUint();
            int readFieldId = (header >>> 3);
            SchemaItem subItem = fieldItemMap.get(readFieldId);
            SchemaItem.EncodeType encodeType = SchemaItem.EncodeType.getType((byte) (header & 0x07));
            int subEndposition = 0;
            if (encodeType != null && encodeType.equals(SchemaItem.EncodeType.LENGTH_DELIMITED)) {
                subEndposition = bb.readUint() + bb.getPosition();
            }
            AutoBufObject subMsg = decodeMessage(bb, subItem, subEndposition);
            embedded.put(subItem.getKey(), subMsg);
        }
        return embedded;
    }

    private String findRef(Integer fieldId) {
        for (Map.Entry<String, TreeSet<Integer>> entry : refFieldMap.entrySet()) {
            if (entry.getValue().contains(fieldId)) {
                return entry.getKey();
            }
        }
        throw Sugar.makeThrow("Data format error");
    }

    private AutoBufObject decodeString(String key, ByteBuilder bb, Integer endPosition) {
        try {
            String data = new String(bb.readBytes(endPosition - bb.getPosition()), "UTF-8");
            return new AutoBufObject(key, data, SchemaItem.DecodeType.STRING);
        } catch (UnsupportedEncodingException e) {
            throw Sugar.makeThrow("Unable to parse object");
        }
    }

    public byte[] getHeader() {
        return header;
    }

    public byte[] getBody() {
        return body;
    }
}
