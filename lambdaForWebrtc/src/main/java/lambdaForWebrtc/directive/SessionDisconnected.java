package lambdaForWebrtc.directive;

import com.alibaba.fastjson.JSONObject;
import lambdaForWebrtc.util.JsonUtil;

/**
 * @ClassName SessionDisconnected
 * @Description TODO
 * @Author huangqh
 * @CreateDate: 2018/12/12 15:49
 **/
public class SessionDisconnected {

    public static JSONObject getSessionDisconnectedResponse(String messageId, String correlationToken, String endpointId, String sessionId){

        JSONObject sessionDisconnectedResponse = JSONObject.parseObject(sessionDisconnectedJson);
        JsonUtil.setJsonValue(sessionDisconnectedResponse,messageId,"event","header","messageId");
        JsonUtil.setJsonValue(sessionDisconnectedResponse,correlationToken,"event","header","correlationToken");
        JsonUtil.setJsonValue(sessionDisconnectedResponse,endpointId,"event","endpoint","endpointId");
        JsonUtil.setJsonValue(sessionDisconnectedResponse,sessionId,"event","payload","sessionId");
        return sessionDisconnectedResponse;

    }

    private static String sessionDisconnectedJson = "{\n" +
            "  \"event\": {\n" +
            "    \"header\": {\n" +
            "      \"namespace\": \"Alexa.RTCSessionController\",\n" +
            "      \"name\": \"SessionDisconnected\",\n" +
            "      \"messageId\": \"30d2cd1a-ce4f-4542-aa5e-04bd0a6492d5\",\n" +
            "      \"correlationToken\": \"dFMb0z+PgpgdDmluhJ1LddFvSqZ/jCc8ptlAKulUj90jSqg==\",\n" +
            "      \"payloadVersion\": \"3\"\n" +
            "    },\n" +
            "    \"endpoint\": {\n" +
            "       \"endpointId\" : \"appliance-001\"\n" +
            "    },\n" +
            "    \"payload\": {\n" +
            "        \"sessionId\" : \"session identifier\"\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
