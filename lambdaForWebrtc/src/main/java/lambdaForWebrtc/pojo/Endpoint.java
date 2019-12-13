package lambdaForWebrtc.pojo;

import java.util.ArrayList;
import java.util.HashMap;

public class Endpoint {
    public Endpoint(){

    }
    private String endpointId;
    private Scope scope;

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public HashMap<String, String> getCookie() {
        return cookie;
    }

    public void setCookie(HashMap<String, String> cookie) {
        this.cookie = cookie;
    }

    private HashMap<String,String> cookie = new HashMap<String, String>();

    private String manufacturerName;
    private String modelName;
    private String friendlyName;
    private String description;

    public ArrayList<Capabilities> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(ArrayList<Capabilities> capabilities) {
        this.capabilities = capabilities;
    }

    //private ArrayList<Capabilities> capabilities = new ArrayList<Capabilities>();
    private ArrayList<Capabilities> capabilities;


    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getDisplayCategories() {
        return displayCategories;
    }

    public void setDisplayCategories(ArrayList<String> displayCategories) {
        this.displayCategories = displayCategories;
    }

    //private ArrayList<String> displayCategories = new ArrayList<String>();
    private ArrayList<String> displayCategories ;

}
