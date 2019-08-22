package cn.autolabor.util.json;

import cn.autolabor.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.autolabor.util.Sugar.makeThrow;

public class JsonNode {

    private String key;
    private Object value;
    private NodeType nodeType;
    public JsonNode() {
    }

    public JsonNode(String key, Object value, NodeType nodeType) {
        this.key = key;
        this.value = value;
        this.nodeType = nodeType;
    }

    public String getString(String... key) {
        JsonNode jsonNode = getNode(key);
        switch (jsonNode.nodeType) {
            case BOOLEAN:
            case STRING:
            case NUMBER_INT:
            case NUMBER_DOUBLE:
                return jsonNode.getValue().toString();
            case ARRAY:
            case MAP:
                return jsonNode.prettyPrint(0, false);
            case NULL:
                return "null";
            default:
                throw makeThrow("%s\n unable to cast to String", jsonNode.prettyPrint(0, true));
        }
    }

    public Boolean getBoolean(String... key) {
        JsonNode jsonNode = getNode(key);
        switch (jsonNode.nodeType) {
            case BOOLEAN:
                return (boolean) jsonNode.getValue();
            default:
                throw makeThrow("%s\n unable to cast to Boolean", jsonNode.prettyPrint(0, true));
        }
    }

    public Double getDouble(String... key) {
        JsonNode jsonNode = getNode(key);
        switch (jsonNode.nodeType) {
            case NUMBER_INT:
            case NUMBER_DOUBLE:
                return (double) jsonNode.getValue();
            default:
                throw makeThrow("%s\n unable to cast to Double", jsonNode.prettyPrint(0, true));
        }
    }

    public Integer getInteger(String... key) {
        JsonNode jsonNode = getNode(key);
        switch (jsonNode.nodeType) {
            case NUMBER_INT:
                return (int) jsonNode.getValue();
            case NUMBER_DOUBLE:
                return (int) Math.round((double) jsonNode.getValue());
            default:
                throw makeThrow("%s\n unable to cast to Integer", jsonNode.prettyPrint(0, true));
        }
    }

    public List getList(String... key) {
        JsonNode jsonNode = getNode(key);
        switch (jsonNode.nodeType) {
            case ARRAY:
                return (List) jsonNode.getValue();
            default:
                throw makeThrow("%s\n unable to cast to List", jsonNode.prettyPrint(0, true));
        }
    }

    public List getListRaw(String... key) {
        List list = getList(key);
        List<Object> output = new ArrayList<>();
        for (Object aList : list) {
            JsonNode subNode = (JsonNode) aList;
            switch (subNode.nodeType) {
                case BOOLEAN:
                    output.add(subNode.getBoolean());
                    break;
                case STRING:
                    output.add(subNode.getString());
                    break;
                case NUMBER_INT:
                    output.add(subNode.getInteger());
                    break;
                case NUMBER_DOUBLE:
                    output.add(subNode.getDouble());
                    break;
                case ARRAY:
                    output.add(subNode.getListRaw());
                    break;
                case MAP:
                    output.add(subNode.getMapRaw());
                    break;
                case NULL:
                    output.add(null);
                    break;
            }
        }
        return output;
    }

    public Map getMap(String... key) {
        JsonNode jsonNode = getNode(key);
        switch (jsonNode.nodeType) {
            case MAP:
                return (Map) jsonNode.getValue();
            default:
                throw makeThrow("%s\n unable to cast to Map", jsonNode.prettyPrint(0, true));
        }
    }

    public Map getMapRaw(String... key) {
        Map map = getMap(key);
        Map<String, Object> output = new HashMap<>();
        for (Object subValue : map.values()) {
            JsonNode subMap = (JsonNode) subValue;
            switch (subMap.getNodeType()) {
                case BOOLEAN:
                    output.put(subMap.getKey(), subMap.getBoolean());
                    break;
                case STRING:
                    output.put(subMap.getKey(), subMap.getString());
                    break;
                case NUMBER_INT:
                    output.put(subMap.getKey(), subMap.getInteger());
                    break;
                case NUMBER_DOUBLE:
                    output.put(subMap.getKey(), subMap.getDouble());
                    break;
                case ARRAY:
                    output.put(subMap.getKey(), subMap.getListRaw());
                    break;
                case MAP:
                    output.put(subMap.getKey(), subMap.getMapRaw());
                    break;
                case NULL:
                    output.put(subMap.getKey(), null);
                    break;
            }
        }
        return output;
    }

    private JsonNode getNode(String... key) {
        if (key == null || key.length == 0) {
            return getNode(".");
        } else {
            return getNode(key[0]);
        }
    }

    public JsonNode getNode(String key) {
        if (key == null || key.equals(".")) {
            return this;
        } else {
            String fKey;
            String bKey;
            if (key.contains(".")) {
                int index = key.indexOf(".");
                fKey = key.substring(0, index);
                bKey = key.substring(index + 1);
            } else {
                fKey = key;
                bKey = null;
            }

            if (Strings.isInteger(fKey) && value != null && nodeType.equals(NodeType.ARRAY)) {
                JsonNode subNode = (JsonNode) ((List) value).get(Integer.parseInt(fKey));
                return subNode.getNode(bKey);
            } else if (value != null && nodeType.equals(NodeType.MAP)) {
                JsonNode subNode = (JsonNode) ((Map) value).get(fKey);
                return subNode.getNode(bKey);
            } else {
                throw makeThrow("invalid key");
            }
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    @SuppressWarnings("unchecked")
    public String prettyPrint(int level, boolean spaceEnable) {
        StringBuilder sb = new StringBuilder();
        spaceForPrint(sb, level, spaceEnable);
        switch (nodeType) {
            case BOOLEAN:
            case NUMBER_INT:
            case NUMBER_DOUBLE:
            case NULL:
                sb.append(key == null ? String.format("%s", value) : String.format("\"%s\": %s", key, value));
                break;
            case STRING:
                sb.append(key == null ? String.format("\"%s\"", value) : String.format("\"%s\": \"%s\"", key, value));
                break;
            case ARRAY:
                sb.append(key == null ? "[" : String.format("\"%s\": [", key));
                List<JsonNode> list = (List<JsonNode>) value;
                for (JsonNode aList : list) {
                    sb.append(aList.prettyPrint(level + 1, spaceEnable)).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                spaceForPrint(sb, level, spaceEnable);
                sb.append("]");
                break;
            case MAP:
                sb.append(key == null ? "{" : String.format("\"%s\": {", key));
                Map<String, JsonNode> map = (Map<String, JsonNode>) value;
                for (JsonNode subMap : map.values()) {
                    sb.append(subMap.prettyPrint(level + 1, spaceEnable)).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                spaceForPrint(sb, level, spaceEnable);
                sb.append("}");
                break;
            default:
                sb.append(key == null ? "null" : String.format("\"%s\": null", key));
        }
        return sb.toString();
    }

    private void spaceForPrint(StringBuilder sb, int level, boolean spaceEnable) {
        if (spaceEnable) {
            sb.append("\n");
            sb.append(Strings.repeat("\t", level));
        }
    }

    @Override
    public String toString() {
        return "JsonNode{" + "key='" + key + '\'' + ", value=" + value + ", nodeType=" + nodeType + '}';
    }

    public enum NodeType {
        BOOLEAN, STRING, NUMBER_INT, NUMBER_DOUBLE, ARRAY, MAP, NULL
    }
}
