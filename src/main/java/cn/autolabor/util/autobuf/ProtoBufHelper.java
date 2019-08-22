package cn.autolabor.util.autobuf;

import cn.autolabor.message.navigation.Msg2DOdometry;
import cn.autolabor.util.Strings;
import cn.autolabor.util.Sugar;
import cn.autolabor.util.collections.Pair;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public final class ProtoBufHelper {

    private AutoBufAdapter adapter;
    private StringBuilder msgStringBuilder = new StringBuilder();
    private Queue<Pair<String, String>> queue = new LinkedList<>(); // REF -- MessageName
    private Set<String> processedRef = new HashSet<>(); // REF


    public ProtoBufHelper(AutoBufAdapter adapter, String packageName) {
        this.adapter = adapter;
        fillHeader(packageName);
        fillMessage("Root", "0", 0, false);
        while (!queue.isEmpty()) {
            Pair<String, String> embeddedInfo = queue.poll();
            fillMessage(embeddedInfo.getValue(), embeddedInfo.getKey(), 0, false);
        }
    }

    public ProtoBufHelper(AutoBufAdapter adapter, String packageName, String rootName) {
        this.adapter = adapter;
        fillHeader(packageName);
        fillMessage(rootName, "0", 0, false);
        while (!queue.isEmpty()) {
            Pair<String, String> embeddedInfo = queue.poll();
            fillMessage(embeddedInfo.getValue(), embeddedInfo.getKey(), 0, false);
        }
    }

    public static void main(String[] args) {
        AutoBufAdapter adapter = new AutoBufAdapter(Msg2DOdometry.class);
        ProtoBufHelper helper = new ProtoBufHelper(adapter, "cn.autolabor.message.Msg2DOdometry");
        helper.printMessage();
    }

    public void printMessage() {
        System.out.println(msgStringBuilder.toString());
    }

    public String getMessageStr() {
        return msgStringBuilder.toString();
    }

    private void fillMessage(String messageName, String ref, int tabNum, boolean isRepeated) {
        String tabString = Strings.repeat("\t", tabNum);
        msgStringBuilder.append(tabString).append(String.format("message %s {\n", messageName));
        Set<Integer> fields = adapter.refFieldMap.get(ref);
        for (Integer field : fields) {
            SchemaItem item = adapter.fieldItemMap.get(field);
            FieldInfo info = digfieldInfo(item, ref.equals("0") ? null : messageName);
            msgStringBuilder.append(tabString).append(String.format("\t%s%s %s = %d;\n", isRepeated ? "repeated " : "", info.typeName, info.fieldName, field));
            if (info.userDefine) {
                String subRef = item.getEmbeddedRef();
                if (info.isEmbedded) {
                    if (!processedRef.contains(subRef)) {
                        queue.offer(new Pair<>(subRef, info.typeName));
                        processedRef.add(subRef);
                    }
                } else {
                    msgStringBuilder.append("\n");
                    fillMessage(info.typeName, subRef, tabNum + 1, true);
                }
            }
        }
        msgStringBuilder.append(tabString).append("}").append("\n\n");
    }

    private void fillHeader(String packageName) {
        msgStringBuilder.append("syntax = \"proto3\";\n\n");
        msgStringBuilder.append("import \"google/protobuf/descriptor.proto\";\n\n");
        msgStringBuilder.append(String.format("package %s;\n\n", Strings.isBlank(packageName) ? "cn.autolabor.message.user" : packageName));
        msgStringBuilder.append("extend google.protobuf.FileOptions {\n");
        msgStringBuilder.append("\t").append("bytes header = 50000;\n").append("}\n\n");
        msgStringBuilder.append("option (header) = \"");
        msgStringBuilder.append(Strings.formatByteToHexString(adapter.getHeader(), "\\x", "", 0)).append("\";\n\n");


    }

    private FieldInfo digfieldInfo(SchemaItem item, String messageName) {
        String key = item.getKey();

        switch (item.getDecodeType()) {
            case BOOLEAN:
                return new FieldInfo(false, false, "bool", Strings.lowerFirst(autoSelectName(key, messageName, "Boolean")));
            case INT32:
                return new FieldInfo(false, false, "fixed32", Strings.lowerFirst(autoSelectName(key, messageName, "Integer")));
            case UINT32:
                return new FieldInfo(false, false, "uint32", Strings.lowerFirst(autoSelectName(key, messageName, "Integer")));
            case SINT32:
                return new FieldInfo(false, false, "sint32", Strings.lowerFirst(autoSelectName(key, messageName, "Integer")));
            case INT64:
                return new FieldInfo(false, false, "fixed64", Strings.lowerFirst(autoSelectName(key, messageName, "Long")));
            case UINT64:
                return new FieldInfo(false, false, "uint64", Strings.lowerFirst(autoSelectName(key, messageName, "Long")));
            case SINT64:
                return new FieldInfo(false, false, "sint64", Strings.lowerFirst(autoSelectName(key, messageName, "Long")));
            case FLOAT:
                return new FieldInfo(false, false, "float", Strings.lowerFirst(autoSelectName(key, messageName, "Float")));
            case DOUBLE:
                return new FieldInfo(false, false, "double", Strings.lowerFirst(autoSelectName(key, messageName, "Double")));
            case STRING:
                return new FieldInfo(false, false, "string", Strings.lowerFirst(autoSelectName(key, messageName, "String")));
            case EMBEDDED:
                String embeddedString = defaultFieldName(item, "");
                return new FieldInfo(true, true, Strings.lastClassName(adapter.refLink.get(item.getEmbeddedRef()).getName()), Strings.lowerFirst(autoSelectName(key, messageName, embeddedString)));
            case LIST:
            case ARRAY:
                SchemaItem subListItem = adapter.fieldItemMap.get(adapter.refFieldMap.get(item.getEmbeddedRef()).iterator().next());
                SchemaItem.DecodeType subType = subListItem.getDecodeType();
                if (subType == SchemaItem.DecodeType.BOOLEAN || subType == SchemaItem.DecodeType.INT16
                        || subType == SchemaItem.DecodeType.UINT16 || subType == SchemaItem.DecodeType.INT32
                        || subType == SchemaItem.DecodeType.UINT32 || subType == SchemaItem.DecodeType.SINT32
                        || subType == SchemaItem.DecodeType.INT64 || subType == SchemaItem.DecodeType.UINT64
                        || subType == SchemaItem.DecodeType.SINT64 || subType == SchemaItem.DecodeType.FLOAT
                        || subType == SchemaItem.DecodeType.DOUBLE) {
                    return new FieldInfo(false, false, String.format("%s %s", "repeated", decodeTypeMapProtoType(subType)), Strings.lowerFirst(autoSelectName(key, messageName, defaultFieldName(item, ""))));
                } else if (subType == SchemaItem.DecodeType.STRING || subType == SchemaItem.DecodeType.LIST
                        || subType == SchemaItem.DecodeType.ARRAY || subType == SchemaItem.DecodeType.EMBEDDED) {
                    String defaultString = defaultFieldName(item, "");
                    return new FieldInfo(false, true, Strings.upperFirst(defaultString), Strings.lowerFirst(autoSelectName(key, messageName, defaultString)));
                }
            default:
                throw Sugar.makeThrow("The Type does not support for conversion");
        }
    }

    private String autoSelectName(String key, String ref, String defaultName) {
        if (Strings.isNotBlank(key)) {
            return key;
        } else if (Strings.isNotBlank(ref)) {
            return String.format("Sub%s", ref);
        } else {
            return defaultName;
        }
    }

    private String defaultFieldName(SchemaItem item, String name) {
        if (Strings.isNotBlank(item.getKey())) {
            return item.getKey();
        } else {
            switch (item.getDecodeType()) {
                case BOOLEAN:
                    return String.format("%s%s", "Boolean", name);
                case BYTE:
                    return String.format("%s%s", "Byte", name);
                case INT16:
                case UINT16:
                    return String.format("%s%s", "Short", name);
                case CHAR:
                    return String.format("%s%s", "Char", name);
                case INT32:
                case UINT32:
                case SINT32:
                    return String.format("%s%s", "Int", name);
                case INT64:
                case UINT64:
                case SINT64:
                    return String.format("%s%s", "Long", name);
                case FLOAT:
                    return String.format("%s%s", "Float", name);
                case DOUBLE:
                    return String.format("%s%s", "Double", name);
                case STRING:
                    return String.format("%s%s", "String", name);
                case EMBEDDED:
                    return String.format("%s%s", Strings.lastClassName(adapter.refLink.get(item.getEmbeddedRef()).getName()), name);
                case LIST:
                    SchemaItem subListItem = adapter.fieldItemMap.get(adapter.refFieldMap.get(item.getEmbeddedRef()).iterator().next());
                    return String.format("%s%s", defaultFieldName(subListItem, name), "List");
                case ARRAY:
                    SchemaItem subArrayItem = adapter.fieldItemMap.get(adapter.refFieldMap.get(item.getEmbeddedRef()).iterator().next());
                    return String.format("%s%s", defaultFieldName(subArrayItem, name), "Array");
                default:
                    throw Sugar.makeThrow("Does not support MAP data conversion");
            }
        }
    }

    private String decodeTypeMapProtoType(SchemaItem.DecodeType type) {
        switch (type) {
            case BOOLEAN:
                return "bool";
            case INT32:
                return "fixed32";
            case UINT32:
                return "uint32";
            case SINT32:
                return "sint32";
            case INT64:
                return "fixed64";
            case UINT64:
                return "uint64";
            case SINT64:
                return "sint64";
            case FLOAT:
                return "float";
            case DOUBLE:
                return "double";
            case STRING:
                return "string";
            default:
                throw Sugar.makeThrow("The Type does not support for conversion");
        }
    }

    class FieldInfo {
        boolean isEmbedded;
        boolean userDefine;
        String typeName;
        String fieldName;

        FieldInfo(boolean isEmbedded, boolean userDefine, String typeName, String fieldName) {
            this.isEmbedded = isEmbedded;
            this.userDefine = userDefine;
            this.typeName = typeName;
            this.fieldName = fieldName;
        }
    }

}
