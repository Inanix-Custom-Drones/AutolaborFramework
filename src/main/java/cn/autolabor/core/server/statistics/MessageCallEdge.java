package cn.autolabor.core.server.statistics;

import cn.autolabor.util.collections.graph.Edge;

public class MessageCallEdge extends Edge {

    private String topic;

    public MessageCallEdge(String topic) {
        super(topic);
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        MessageCallEdge that = (MessageCallEdge) o;

        return topic.equals(that.topic);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + topic.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MessageCallEdge{" + "topic='" + topic + '\'' + '}';
    }
}
