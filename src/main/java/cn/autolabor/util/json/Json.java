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
        String str = "urdf : {\n" + "    baseLink : {\n" + "        lidar : {\n" + "            x : 0.0\n" + "            y : 0.0\n" + "            theta : 0.0\n" + "            reverse : false\n" + "        }\n" + "    }\n" + "}\n" + "\n" + "ObstacleDetectionTask : {\n" + "    clusterMinPoints : 2       # 成为核心节点周围至少需要多少个点，包括自己\n" + "    clusterMaxPoints : 30      # 一个点簇最多包含几个点\n" + "    clusterMaxDistance : 0.4   # 点与点之间的最大距离\n" + "    baseLinkFrame : \"baseLink\"   # 小车自身坐标系\n" + "    lidarTopics : [\"scan_out\"] # 雷达话题名称\n" + "    timeout : 50               # 多雷达匹配最大时间间隔\n" + "}\n" + "\n" + "PoseDetectionTask : {\n" + "    outline : [[0.4,0.2],[0.4,-0.2],[-0.4,-0.2],[-0.4,0.2]]   # 车轮廓\n" + "    baseLinkFrame : \"baseLink\"                                  # 小车自身坐标系\n" + "    predictionTime : 0.3                                      # 预估该时间后车的位姿，用来做障碍物碰撞检测\n" + "    deltaRotation : 0.2                                      # 小车旋转测试时，转角测试间隔\n" + "    deltaOmega : 0.05                                         # 选取最优角速度时，角速度采样间隔\n" + "    deltaNumber : 3                                           # 采样个数\n" + "}";
        List<JsonNode> list = fromJsons(str);
        list.forEach(i -> System.out.println(i.prettyPrint(0, true)));
    }

}
