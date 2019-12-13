package pojo;

import com.alibaba.fastjson.annotation.JSONField;

public class Event {
    public Event(){

    }
    @JSONField(ordinal=1)
    private Header header;
    @JSONField(ordinal=2)
    private Endpoint endpoint;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    @JSONField(ordinal=3)
    private Payload payload;
}
