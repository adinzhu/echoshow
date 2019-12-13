package pojo;

import java.util.UUID;

public class Response {

    public Response(Request request){
        this.event = new Event();
        this.event.setHeader(request.getDirective().getHeader());
        this.event.getHeader().setMessageId(UUID.randomUUID().toString());

        //this.event.setEndpoint(new Endpoint());
        //this.event.getEndpoint().setEndpointId(request.getDirective().getEndpoint().getEndpointId());
    }
    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    private Event event;
}
