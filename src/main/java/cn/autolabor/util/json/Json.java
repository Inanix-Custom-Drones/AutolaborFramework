package cn.autolabor.util.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Json {

    public static JsonNode fromJson(String jsonString) {
        JsonParser jsonParser = new JsonParser(jsonString);
        return jsonParser.parseNode();
    }

    public static List<JsonNode> fromJsons(String jsonString) {
        List<JsonNode> jsonNodes = new ArrayList<>();
        JsonParser jsonParser = new JsonParser(jsonString);
        while (true) {
            JsonNode jsonNode = jsonParser.parseNode();
            if (jsonNode != null) {
                jsonNodes.add(jsonNode);
            } else {
                break;
            }
        }
        return jsonNodes;
    }

    public static String toJson(JsonNode jsonNode) {
        return jsonNode.prettyPrint(0, false);
    }

    public static JsonNode toJsonNode(String key, Object data) {
        JsonNode jsonNode = new JsonNode();
        jsonNode.setKey(key);
        if (String.class.isInstance(data)) {
            jsonNode.setNodeType(JsonNode.NodeType.STRING);
            jsonNode.setValue(data);
        } else if (Integer.class.isInstance(data) || int.class.isInstance(data)) {
            jsonNode.setNodeType(JsonNode.NodeType.NUMBER_INT);
            jsonNode.setValue(data);
            return jsonNode;
        } else if (Double.class.isInstance(data) || double.class.isInstance(data)) {
            jsonNode.setNodeType(JsonNode.NodeType.NUMBER_DOUBLE);
            jsonNode.setValue(data);
        } else if (Boolean.class.isInstance(data) || boolean.class.isInstance(data)) {
            jsonNode.setNodeType(JsonNode.NodeType.BOOLEAN);
            jsonNode.setValue(data);
        } else if (List.class.isInstance(data)) {
            jsonNode.setNodeType(JsonNode.NodeType.ARRAY);
            List<JsonNode> list = new ArrayList<>();
            for (Object o : (List) data) {
                list.add(toJsonNode(null, o));
            }
            jsonNode.setValue(list);
        } else if (Map.class.isInstance(data)) {
            jsonNode.setNodeType(JsonNode.NodeType.MAP);
            Map<String, JsonNode> map = new HashMap<>();
            for (Object subMap : ((Map) data).entrySet()) {
                Map.Entry entry = (Map.Entry) subMap;
                map.put((String) entry.getKey(), toJsonNode((String) entry.getKey(), entry.getValue()));
            }
            jsonNode.setValue(map);
        } else {
            jsonNode.setNodeType(JsonNode.NodeType.NULL);
            jsonNode.setValue(null);
        }
        return jsonNode;
    }


    public static void main(String[] args) {
        int inumber = 123;
        Double dnumber = 123.45;
        System.out.println((int) Math.round(dnumber));
    }

}
