package cn.autolabor.core.server.executor;


import cn.autolabor.util.lambda.function.TaskLambdaFun;

import java.util.Arrays;

public class ComparableFunctionItem implements Comparable<ComparableFunctionItem> {

    private Long time;
    private String eventName;
    private TaskLambdaFun method;
    private Object[] params;

    public ComparableFunctionItem(Long time, String eventName, TaskLambdaFun method, Object... params) {
        this.time = time;
        this.eventName = eventName;
        this.method = method;
        this.params = params;
    }


    public String toString() {
        return String.format("[Time]: %d; [Method]: %s; [params size]: %d", this.time / 1000000, this.eventName, this
                .params == null ? 0 : this.params.length);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ComparableFunctionItem that = (ComparableFunctionItem) o;

        if (time != null ? !time.equals(that.time) : that.time != null)
            return false;
        if (eventName != null ? !eventName.equals(that.eventName) : that.eventName != null)
            return false;
        if (method != null ? !method.equals(that.method) : that.method != null)
            return false;
        return Arrays.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        int result = time != null ? time.hashCode() : 0;
        result = 31 * result + (eventName != null ? eventName.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(params);
        return result;
    }

    public int compareTo(ComparableFunctionItem o) {
        if (this == o) {
            return 0;
        }
        if (this.getTime() == null || o.getMethod() == null) {
            throw new NullPointerException();
        } else {
            return this.getTime().compareTo(o.getTime());
        }
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public TaskLambdaFun getMethod() {
        return method;
    }

    public void setMethod(TaskLambdaFun method) {
        this.method = method;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object... params) {
        this.params = params;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
