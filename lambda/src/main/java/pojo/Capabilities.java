package pojo;

import java.util.ArrayList;

public class Capabilities {
    private String type;
    private String interface_meari_special;
    private String version;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInterface_meari_special() {
        return interface_meari_special;
    }

    public void setInterface_meari_special(String interface_meari_special) {
        this.interface_meari_special = interface_meari_special;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ArrayList<CameraStreamConfigurations> getCameraStreamConfigurations() {
        return cameraStreamConfigurations;
    }

    public void setCameraStreamConfigurations(ArrayList<CameraStreamConfigurations> cameraStreamConfigurations) {
        this.cameraStreamConfigurations = cameraStreamConfigurations;
    }

    private ArrayList<CameraStreamConfigurations> cameraStreamConfigurations = new ArrayList<CameraStreamConfigurations>();
}
