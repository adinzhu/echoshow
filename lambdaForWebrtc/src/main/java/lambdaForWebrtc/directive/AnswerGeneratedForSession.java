package lambdaForWebrtc.directive;

import com.alibaba.fastjson.JSONObject;
import lambdaForWebrtc.util.JsonUtil;

/**
 * @ClassName AnswerGeneratedForSession
 * @Description TODO
 * @Author huangqh
 * @CreateDate: 2018/12/12 11:42
 **/
public class AnswerGeneratedForSession {

    public static JSONObject getAnswerGeneratedForSessionResponse(String messageId,String correlationToken,String endpointId,String sdpValue){

        JSONObject answerGeneratedForSession = JSONObject.parseObject(answerGeneratedForSessionJson);
        JsonUtil.setJsonValue(answerGeneratedForSession,messageId,"event","header","messageId");
        JsonUtil.setJsonValue(answerGeneratedForSession,correlationToken,"event","header","correlationToken");
        JsonUtil.setJsonValue(answerGeneratedForSession,endpointId,"event","endpoint","endpointId");
        JsonUtil.setJsonValue(answerGeneratedForSession,sdpValue,"event","payload","answer","value");
        return answerGeneratedForSession;

    }

    private static String answerGeneratedForSessionJson = "{\n" +
            "    \"event\": {\n" +
            "        \"header\": {\n" +
            "            \"namespace\": \"Alexa.RTCSessionController\",\n" +
            "            \"name\": \"AnswerGeneratedForSession\",\n" +
            "            \"messageId\": \"30d2cd1a-ce4f-4542-aa5e-04bd0a6492d5\",\n" +
            "            \"correlationToken\": \"dFMb0z+PgpgdDmluhJ1LddFvSqZ/jCc8ptlAKulUj90jSqg==\",\n" +
            "            \"payloadVersion\": \"3\"\n" +
            "        },\n" +
            "        \"endpoint\": {\n" +
            "            \"endpointId\" : \"appliance-001\",\n" +
            "        },\n" +
            "        \"payload\": {\n" +
            "            \"answer\": {\n" +
            "                \"format\" : \"SDP\",\n" +
            "                \"value\" : \"\"\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
}
