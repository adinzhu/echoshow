package lambdaForWebrtc.directive;

import com.alibaba.fastjson.JSONObject;
import lambdaForWebrtc.util.JsonUtil;

/**
 * @ClassName ErrorResponse
 * @Description TODO
 * @Author huangqh
 * @CreateDate: 2018/12/12 19:04
 **/
public class ErrorResponse {

    public static JSONObject getSessionConnectedResponse(
            String messageId,
            String correlationToken,
            String endpointId,
            String type,
            String message){

        JSONObject errorResponse = JSONObject.parseObject(errorJson);
        JsonUtil.setJsonValue(errorResponse,messageId,"event","header","messageId");
        JsonUtil.setJsonValue(errorResponse,correlationToken,"event","header","correlationToken");
        JsonUtil.setJsonValue(errorResponse,endpointId,"event","endpoint","endpointId");
        JsonUtil.setJsonValue(errorResponse,type,"event","payload","type");
        JsonUtil.setJsonValue(errorResponse,message,"event","payload","message");
        return errorResponse;

    }

    private static String errorJson = "{\n" +
            "\"event\": {\n" +
            "    \"header\": {\n" +
            "      \"namespace\": \"Alexa\",\n" +
            "      \"name\": \"ErrorResponse\",\n" +
            "      \"messageId\": \"abc-123-def-456\",\n" +
            "      \"correlationToken\": \"dFMb0z+PgpgdDmluhJ1LddFvSqZ/jCc8ptlAKulUj90jSqg==\",\n" +
            "      \"payloadVersion\": \"3\"\n" +
            "    },\n" +
            "    \"endpoint\":{\n" +
            "        \"endpointId\":\"appliance-001\"\n" +
            "    },\n" +
            "    \"payload\": {\n" +
            "      \"type\": \"ENDPOINT_UNREACHABLE\",\n" +
            "      \"message\": \"Unable to reach endpoint 12345 because it appears to be offline\"\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
