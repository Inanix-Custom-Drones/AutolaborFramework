package cn.autolabor.core.server.message;

public class MessageSource {

    private String identification;
    private String topic;
    private MessageSourceType sourceType;
    private String ip;
    private Integer port;

    public MessageSource(String identification, String topic, MessageSourceType sourceType, String ip, Integer port) {
        this.identification = identification;
        this.topic = topic;
        this.sourceType = sourceType;
        this.ip = ip;
        this.port = port;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public MessageSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(MessageSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return "MessageSource{" +
                "identification='" + identification + '\'' +
                ", topic='" + topic + '\'' +
                ", sourceType=" + sourceType +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
