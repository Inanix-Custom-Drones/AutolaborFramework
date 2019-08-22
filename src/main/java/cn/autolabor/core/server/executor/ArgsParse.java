package cn.autolabor.core.server.executor;

import cn.autolabor.util.Sugar;
import cn.autolabor.util.json.Json;
import cn.autolabor.util.json.JsonNode;

public class ArgsParse {

    private String taskName = null;
    private Integer priority = null;
    private Boolean unique = null;
    private Boolean preemptive = null;
    private Boolean silentInit = null;
    private Boolean debug = null;

    ArgsParse(String[] args) {
        if (args != null && args.length > 0) {
            int startIndex = 0;
            if (!args[0].contains("=")) {
                this.taskName = args[0];
                startIndex = 1;
            }

            for (int i = startIndex; i < args.length; i++) {
                JsonNode jsonNode = Json.fromJson(args[i]);
                if (jsonNode.getKey().equalsIgnoreCase("priority")) {
                    this.priority = (Integer) jsonNode.getValue();
                } else if (jsonNode.getKey().equalsIgnoreCase("unique")) {
                    this.unique = (Boolean) jsonNode.getValue();
                } else if (jsonNode.getKey().equalsIgnoreCase("preemptive")) {
                    this.preemptive = (Boolean) jsonNode.getValue();
                } else if (jsonNode.getKey().equalsIgnoreCase("silentInit")) {
                    this.silentInit = (Boolean) jsonNode.getValue();
                } else if (jsonNode.getKey().equalsIgnoreCase("debug")) {
                    this.debug = (Boolean) jsonNode.getValue();
                } else {
                    throw Sugar.makeThrow("Task parameter name %s is not recognized", jsonNode.getKey());
                }
            }
        }
    }

    public String getTaskNameOrDefault(String defaultTaskName) {
        return this.taskName == null ? defaultTaskName : this.taskName;
    }

    public Integer getPriorityOrDefault(Integer defaultPriority) {
        return this.priority == null ? defaultPriority : this.priority;
    }

    public Boolean getUniqueOrDefault(Boolean defaultUnique) {
        return this.unique == null ? defaultUnique : this.unique;
    }

    public Boolean getPreemptiveOrDefault(Boolean defaultPreemptive) {
        return this.preemptive == null ? defaultPreemptive : this.preemptive;
    }

    public Boolean getSilentInitOrDefault(Boolean defaultSilentInit) {
        return this.silentInit == null ? defaultSilentInit : this.silentInit;
    }

    public Boolean getDebugOrDefault(Boolean defaultDebug) {
        return this.debug == null ? defaultDebug : this.debug;
    }
}
