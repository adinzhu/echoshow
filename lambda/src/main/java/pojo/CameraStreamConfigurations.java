package pojo;

import java.util.ArrayList;

public class CameraStreamConfigurations {
    private ArrayList<String> protocols = new ArrayList<String>();
    private ArrayList<String> authorizationTypes = new ArrayList<String>();
    private ArrayList<String> videoCodecs = new ArrayList<String>();
    private ArrayList<String> audioCodecs = new ArrayList<String>();


    public ArrayList<String> getProtocols() {
        return protocols;
    }

    public void setProtocols(ArrayList<String> protocols) {
        this.protocols = protocols;
    }

    public ArrayList<String> getAuthorizationTypes() {
        return authorizationTypes;
    }

    public void setAuthorizationTypes(ArrayList<String> authorizationTypes) {
        this.authorizationTypes = authorizationTypes;
    }

    public ArrayList<String> getVideoCodecs() {
        return videoCodecs;
    }

    public void setVideoCodecs(ArrayList<String> videoCodecs) {
        this.videoCodecs = videoCodecs;
    }

    public ArrayList<String> getAudioCodecs() {
        return audioCodecs;
    }

    public void setAudioCodecs(ArrayList<String> audioCodecs) {
        this.audioCodecs = audioCodecs;
    }

    public ArrayList<Resolution> getResolutions() {
        return resolutions;
    }

    public void setResolutions(ArrayList<Resolution> resolutions) {
        this.resolutions = resolutions;
    }

    public ArrayList<Resolution> resolutions = new ArrayList<Resolution>();
}
