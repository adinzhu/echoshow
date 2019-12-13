package lambdaForWebrtc.pojo;

import java.util.ArrayList;

public class Payload {
    public Payload(){

    }
    public ArrayList<CameraStreams> getCameraStreams() {
        return cameraStreams;
    }

    public void setCameraStreams(ArrayList<CameraStreams> cameraStreams) {
        this.cameraStreams = cameraStreams;
    }

    private ArrayList<CameraStreams> cameraStreams = new ArrayList<CameraStreams>() ;

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    private String imageUri;

    public ArrayList<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(ArrayList<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    //private ArrayList<Endpoint> endpoints = new ArrayList<Endpoint>();

    private ArrayList<Endpoint> endpoints;

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    private  Scope scope;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String type;

    private String message;


}
