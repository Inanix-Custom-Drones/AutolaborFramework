package cn.autolabor.core.server.statistics;

import cn.autolabor.util.autobuf.SerializableMessage;

public class EdgeMessage implements SerializableMessage {

    private String type;
    private String param;

    public EdgeMessage(String type, String param) {
        this.type = type;
        this.param = param;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        EdgeMessage that = (EdgeMessage) o;

        if (!type.equals(that.type))
            return false;
        return param != null ? param.equals(that.param) : that.param == null;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (param != null ? param.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EdgeMessage{" + "type='" + type + '\'' + ", param='" + param + '\'' + '}';
    }
}
