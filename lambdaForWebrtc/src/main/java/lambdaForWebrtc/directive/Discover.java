package lambdaForWebrtc.directive;

import com.alibaba.fastjson.JSONObject;
import lambdaForWebrtc.util.JsonUtil;

/**
 * @ClassName Discover
 * @Description TODO
 * @Author huangqh
 * @CreateDate: 2018/12/12 9:50
 **/
public class Discover {

    private JSONObject discover;

    public Discover(String messageId){

        this.discover = JSONObject.parseObject(discoverResponseJson);
        JsonUtil.setJsonValue(this.discover,messageId,"event","header","messageId");
    }

    public Discover addDevice(String productKey,String deviceName,String endpointId,String friendlyName){

        JSONObject endpoint = JSONObject.parseObject(endpointJson);
        JsonUtil.setJsonValue(endpoint,friendlyName,"modelName");
        JsonUtil.setJsonValue(endpoint,friendlyName,"friendlyName");
        JsonUtil.setJsonValue(endpoint,endpointId,"endpointId");
        JsonUtil.setJsonValue(endpoint,friendlyName,"description");
        JsonUtil.setJsonValue(endpoint,productKey,"cookie","productKey");
        JsonUtil.setJsonValue(endpoint,deviceName,"cookie","deviceName");

        JsonUtil.addJsonArrayValue(this.discover,endpoint,"event","payload","endpoints");
        return this;
    }

    public JSONObject getDiscoverResponse(){
        return this.discover;
    }

    private String discoverResponseJson = "{\n" +
            "    \"event\": {\n" +
            "      \"header\": {\n" +
            "        \"namespace\":\"Alexa.Discovery\",\n" +
            "        \"name\":\"Discover.Response\",\n" +
            "        \"payloadVersion\":\"3\",\n" +
            "        \"messageId\":\"ff746d98-ab02-4c9e-9d0d-b44711658414\"\n" +
            "      },\n" +
            "      \"payload\":{\n" +
            "        \"endpoints\":[\n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "}";
    private String endpointJson = "{\n" +
            "            \"manufacturerName\": \"Meari\",\n" +
            "            \"modelName\": \"Sample Model\",\n" +
            "            \"friendlyName\": \"My front door camera\",\n" +
            "            \"description\": \"\",\n" +
            "            \"displayCategories\": [ \"CAMERA\" ],\n" +
            "            \"endpointId\": \"056566733\","+
            "            \"cookie\": {\n" +
            "                \"productKey\": \"\",\n" +
            "                \"deviceName\": \"\",\n" +
            "                \"userDeviceSecret\": \"\",\n" +
            "            },\n" +
            "            \"capabilities\":\n" +
            "            [\n" +
            "              {\n" +
            "                \"type\": \"AlexaInterface\",\n" +
            "                \"interface\": \"Alexa.RTCSessionController\",\n" +
            "                \"version\": \"3\",\n" +
            "                \"configuration\": {\n" +
            "                  \"isFullDuplexAudioSupported\": true\n" +
            "                }\n" +
            "              }\n" +
            "            ]\n" +
            "          }";
}
