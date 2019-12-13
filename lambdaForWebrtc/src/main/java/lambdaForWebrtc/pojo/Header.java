package lambdaForWebrtc.pojo;

public class Header {
    public  Header(){

    }
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPayloadVersion() {
        return payloadVersion;
    }

    public void setPayloadVersion(String payloadVersion) {
        this.payloadVersion = payloadVersion;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCorrelationToken() {
        return correlationToken;
    }

    public void setCorrelationToken(String correlationToken) {
        this.correlationToken = correlationToken;
    }

    private String namespace;
    private String name;
    private String payloadVersion;
    private String messageId;
    private String correlationToken;
}
