package lambdaForWebrtc.directive;

import com.alibaba.fastjson.JSONObject;
import lambdaForWebrtc.util.JsonUtil;

/**
 * @ClassName SessionConnected
 * @Description TODO
 * @Author huangqh
 * @CreateDate: 2018/12/12 15:41
 **/
public class SessionConnected {

    public static JSONObject getSessionConnectedResponse(String messageId,String correlationToken,String endpointId,String sessionId){

        JSONObject sessionConnectedResponse = JSONObject.parseObject(sessionConnectedJson);
        JsonUtil.setJsonValue(sessionConnectedResponse,messageId,"event","header","messageId");
        JsonUtil.setJsonValue(sessionConnectedResponse,correlationToken,"event","header","correlationToken");
        JsonUtil.setJsonValue(sessionConnectedResponse,endpointId,"event","endpoint","endpointId");
        JsonUtil.setJsonValue(sessionConnectedResponse,sessionId,"event","payload","sessionId");
        return sessionConnectedResponse;

    }

    private static String sessionConnectedJson = "{\n" +
            "  \"event\": {\n" +
            "    \"header\": {\n" +
            "      \"namespace\": \"Alexa.RTCSessionController\",\n" +
            "      \"name\": \"SessionConnected\",\n" +
            "      \"messageId\": \"30d2cd1a-ce4f-4542-aa5e-04bd0a6492d5\",\n" +
            "      \"correlationToken\": \"dFMb0z+PgpgdDmluhJ1LddFvSqZ/jCc8ptlAKulUj90jSqg==\",\n" +
            "      \"payloadVersion\": \"3\"\n" +
            "    },\n" +
            "    \"endpoint\": {\n" +
            "       \"endpointId\" :  \"appliance-001\" ,\n" +
            "    },\n" +
            "    \"payload\": {\n" +
            "        \"sessionId\" : \"session identifier\"\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
