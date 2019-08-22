package cn.autolabor.core.server.statistics;

import cn.autolabor.util.autobuf.SerializableMessage;

public class NodeMessage implements SerializableMessage {

    private String type;
    private String param1;
    private String param2;

    public NodeMessage(String type, String param1, String param2) {
        this.type = type;
        this.param1 = param1;
        this.param2 = param2;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParam2() {
        return param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        NodeMessage that = (NodeMessage) o;

        if (!type.equals(that.type))
            return false;
        if (param1 != null ? !param1.equals(that.param1) : that.param1 != null)
            return false;
        return param2 != null ? param2.equals(that.param2) : that.param2 == null;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (param1 != null ? param1.hashCode() : 0);
        result = 31 * result + (param2 != null ? param2.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NodeMessage{" + "type='" + type + '\'' + ", param1='" + param1 + '\'' + ", param2='" + param2 + '\'' + '}';
    }
}
