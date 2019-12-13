package com.meari.echoshow.pojo;

import java.util.HashMap;
import java.util.Map;

public class Token {

    public Token(String access_token, String refresh_token) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
    }

    private String access_token;
    private String refresh_token;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public static Map<String,String> toTokenMap(String access_token, String refresh_token){
        Map<String,String> map = new HashMap<>(2);
        map.put("access_token",access_token);
        map.put("refresh_token",refresh_token);
        return map;
    }

    public static Token toToken(Map<String,String> map){
        if(map != null){
            return new Token(map.get("access_token"),map.get("refresh_token"));
        }else{
            return null;
        }
    }

}
