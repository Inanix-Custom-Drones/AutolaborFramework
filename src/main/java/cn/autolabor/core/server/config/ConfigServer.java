package cn.autolabor.core.server.config;

import cn.autolabor.util.Files;
import cn.autolabor.util.Strings;
import cn.autolabor.util.json.Json;
import cn.autolabor.util.json.JsonNode;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;


public class ConfigServer {

    private ConcurrentMap<String, Map<String, Object>> config;

    public ConfigServer() {
        this.config = new ConcurrentHashMap<>();

        String configPath = null;
        URL pathURL = ClassLoader.getSystemResource("conf/default.conf");
        if (pathURL != null) {
            String urlPath = pathURL.getPath();
            if (Strings.isNotBlank(urlPath)) {
                File file = new File(urlPath);
                if (file.exists() && file.isFile() && file.canRead()) {
                    configPath = urlPath;
                }
            }
        }

        if (configPath == null) {
            String filePath = System.getProperty("user.dir") + "/conf/default.conf";
            File file = new File(filePath);
            if (file.exists() && file.isFile() && file.canRead()) {
                configPath = file.getAbsolutePath();
            }
        }

        if (configPath == null) {
            System.out.println("No default conf");
        } else {
            load(configPath);
        }
    }

    public static void main(String[] args) {
        ConfigServer configServer = new ConfigServer();
        configServer.load(ClassLoader.getSystemResource("conf/default.conf").getPath());
        System.out.println(configServer);
    }

    @SuppressWarnings("unchecked")
    public void load(String filePath) {
        String fileContent = Files.getContents(filePath);
        List<JsonNode> jsonNodes = Json.fromJsons(fileContent);
        if (jsonNodes.size() == 1 && jsonNodes.get(0).getKey() == null && jsonNodes.get(0).getNodeType().equals(JsonNode.NodeType.MAP)) {
            jsonNodes = new ArrayList<>(jsonNodes.get(0).getMap().values());
        }
        for (JsonNode jsonNode : jsonNodes) {
            if (jsonNode.getKey() != null && jsonNode.getNodeType().equals(JsonNode.NodeType.MAP)) {
                String key = jsonNode.getKey();
                config.put(key, merge(config.get(key), (Map<String, Object>) jsonNode.getMapRaw()));
            }
        }
    }

    public void setParam(String taskName, String paramName, Object param) {
        if (this.config.containsKey(taskName)) {
            this.config.get(taskName).put(paramName, param);
        } else {
            Map<String, Object> subMap = new HashMap<>();
            subMap.put(paramName, param);
            this.config.put(taskName, subMap);
        }
    }

    public boolean checkParam(String taskName, String paramName) {
        if (this.config.containsKey(taskName) && this.config.get(taskName).containsKey(paramName)) {
            return true;
        } else {
            for (String key : this.config.keySet()) {
                if (key.contains("?") || key.contains("*")) {
                    if (Pattern.compile(key.replace("*", ".+").replace("?", ".{1}")).matcher(taskName).matches()) {
                        return this.config.get(key).containsKey(paramName);
                    }
                }
            }
        }
        return false;
    }

    public Object getParam(String taskName, String paramName) {
        if (this.config.containsKey(taskName) && this.config.get(taskName).containsKey(paramName)) {
            return this.config.get(taskName).get(paramName);
        } else {
            for (String key : this.config.keySet()) {
                if (key.contains("?") || key.contains("*")) {
                    if (Pattern.compile(key.replace("*", ".+").replace("?", ".{1}")).matcher(taskName).matches()) {
                        return this.config.get(key).get(paramName);
                    }
                }
            }
        }
        return null;
    }

    public void clear() {
        this.config = new ConcurrentHashMap<>();
    }

    public void remove(String taskName) {
        this.config.remove(taskName);
    }

    public String dump() {
        JsonNode jsonNode = Json.toJsonNode(null, this.config);
        return jsonNode.prettyPrint(0, true);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> merge(Map<String, Object> map, Map<String, Object> mergeMap) {
        if (map == null) {
            return mergeMap;
        }
        for (Map.Entry<String, Object> entry : mergeMap.entrySet()) {
            String key = entry.getKey();
            if (map.containsKey(key)) {
                if (!map.get(key).equals(entry.getValue())) {
                    if (Map.class.isInstance(map.get(key)) && Map.class.isInstance(entry.getValue())) {
                        map.put(key, merge((Map<String, Object>) map.get(key), (Map<String, Object>) entry.getValue()));
                    } else {
                        map.put(key, entry.getValue());
                    }
                }
            } else {
                map.put(key, entry.getValue());
            }
        }
        return map;
    }

    @Override
    public String toString() {
        return "ConfigServer{" + "conf=" + config + '}';
    }

}
