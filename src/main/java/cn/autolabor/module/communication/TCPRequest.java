package cn.autolabor.module.communication;

import java.util.LinkedHashMap;
import java.util.Map;

public class TCPRequest {

    int count = 0;
    private String taskName;
    private String eventName;
    private Map<String, Object> params;

    public TCPRequest() {
    }

    public TCPRequest(String taskName, String eventName) {
        this.taskName = taskName;
        this.eventName = eventName;
    }

    public TCPRequest addParam(Object param) {
        if (params == null) {
            params = new LinkedHashMap<>();
        }
        params.put(String.format("arg%s", count++), param);
        return this;
    }

    public boolean hasParam() {
        return params != null;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
