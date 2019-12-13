package lambdaForWebrtc.lambda;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context;
import lambdaForWebrtc.api.DirectiveGetter;
import lambdaForWebrtc.constant.SystemConstant;
import lambdaForWebrtc.directive.*;
import lambdaForWebrtc.pojo.*;
import lambdaForWebrtc.util.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author huangqh
 */
public class CameraHandle  implements  RequestHandler<JSONObject, Object> {

    private String symmetricKey = "FCEEB1B960E91A5AD1EBC29C6B03000B";
    private int millisecondsOfOneDay = 86400*1000;
    private static String EXPIRED_ERROR = "EXPIRED_AUTHORIZATION_CREDENTIAL";
    private static String SUCCESS = "SUCCESS";
    private static String INVALID_AUTHORIZATION = "INVALID_AUTHORIZATION_CREDENTIAL";
    private static String INTERNAL_ERROR = "INTERNAL_ERROR";
    private static String DISCOVER = "Discover";
    private static String INITIATE_SESSION_WITH_OFFER = "InitiateSessionWithOffer";
    private static String SESSION_CONNECTED = "SessionConnected";
    private static String SESSION_DISCONNECTED = "SessionDisconnected";
    private static String SUCCEED_RESULT_CODE = "1001";
    private static String RESULT = "result";
    private static String RESULT_CODE = "resultCode";
    private static String IPC = "ipc";
    private static String MEDIA_SERVER_URL_FROM_BALANCE = "url";
    private String mediaUrlCache = null;

    @Override
    public Object handleRequest(JSONObject request, Context context) {
        System.out.println(request);

        JSONObject response = new JSONObject();
        String directiveType = JsonUtil.findJsonValue(request,"directive","header","name");
        if( DISCOVER.equals(directiveType)){
            response = this.discoverCamera(request);
        }else if(INITIATE_SESSION_WITH_OFFER.equals(directiveType)){
            response = this.changeSdpMessageAndGetResponse(request);

        }else if(SESSION_CONNECTED.equals(directiveType)){
            response = this.getSessionConnectedResponse(request);

        }else if(SESSION_DISCONNECTED.equals(directiveType)){
            response = this.getSessionDisconnectedResponse(request);

        }

        System.out.println(response);
        return response;

    }

    /**
     *  调用echoshow的微服务，根据用户id获取到设备信息，生成Discover指令并返回
     *   传入的discover指令格式为：
     *   {
     *     "directive": {
     *         "header": {
     *             "namespace": "Alexa.Discovery",
     *             "name": "Discover",
     *             "payloadVersion": "3",
     *             "messageId": "07d5208c-694c-4076-99fe-79456197b04f"
     *         },
     *         "payload": {
     *             "cameraStreams": [],
     *             "scope": {
     *                 "type": "BearerToken",
     *                 "token": "gybHkf3hxhOZWvyPPZN61nDRtVwzfE9C6u3BA5qIFamFmM5UEyR8PFlis/W9Mb2Z"
     *             }
     *         }
     *     }
     * }
     * @param request
     * @return
     */
    public JSONObject discoverCamera(JSONObject request){

        String userToken = JsonUtil.findJsonValue(request,"directive","payload","scope","token");
        HashMap<String,String> userInfo = getAndValidUserToken(userToken);
        ArrayList<Map<String,String>> deviceInfo;
        //userInfo中的result属性不等于success，表示认证异常,discover指令返回空的设备
        if(!userInfo.get(RESULT).equals(SUCCESS)){
            deviceInfo = new ArrayList<Map<String,String>>();
        }else{
            deviceInfo = this.getDeviceInfo(userInfo);
        }

        Discover discoverResponse = new Discover(JsonUtil.findJsonValue(request,"directive","header","messageId"));

        for(Map<String,String> application:deviceInfo) {

            String productKey = application.get("tp");
            String deviceName = application.get("snNum");
            String endpointId = String.valueOf(application.get("deviceID"));
            String friendlyName = application.get("deviceName");
            discoverResponse.addDevice(productKey,deviceName,endpointId,friendlyName);
        }

        return discoverResponse.getDiscoverResponse();


    }

    /**
     *  根据InitiateSessionWithOffer 指令调用iothub，获取到设备返回的sdp信息，然后生成AnswerGeneratedForSession 指令，
     *  InitiateSessionWithOffer指令格式为：
     *  {
     *     "directive": {
     *         "header": {
     *           "namespace": "Alexa.RTCSessionController",
     *           "name": "InitiateSessionWithOffer",
     *           "messageId": "d1ba3aa7-bff7-4406-9425-f25f04ec8d68",
     *           "correlationToken": "dFMb0z+PgpgdDmluhJ1LddFvSqZ/jCc8ptlAKulUj90jSqg==",
     *           "payloadVersion": "3"
     *         },
     *         "endpoint": {
     *           "scope": {
     *               "type": "BearerToken",
     *               "token": "access-token-from-skill"
     *             },
     *             "endpointId": "appliance-001",
     *             "cookie": {
     *                 "keys": "key/value pairs received during discovery",
     *               }
     *         },
     *         "payload": {
     *           "sessionId" : "the session identifier",
     *           "offer": {
     *              "format" : "SDP",
     *              "value" : "<SDP offer value>"
     *           }
     *         }
     *     }
     * }
     * @param request
     */
    public JSONObject changeSdpMessageAndGetResponse(JSONObject request){
        String messageId = JsonUtil.findJsonValue(request,"directive","header","messageId");
        String correlationToken = JsonUtil.findJsonValue(request,"directive","header","correlationToken");
        String userToken = JsonUtil.findJsonValue(request,"directive","endpoint","scope","token");
        String endpointId = JsonUtil.findJsonValue(request,"directive","endpoint","endpointId");
        //cookie
        String productKey = JsonUtil.findJsonValue(request,"directive","endpoint","cookie","productKey");
        String deviceName = JsonUtil.findJsonValue(request,"directive","endpoint","cookie","deviceName");

        String sdp = JsonUtil.findJsonValue(request,"directive","payload","offer","value");

        //验证并获取用户信息
        HashMap<String,String> userInfo = getAndValidUserToken(userToken);
        String userDeviceSecret = userInfo.get("userDeviceSecret");
        String userID = userInfo.get("userID");
        String sourceApp = userInfo.get("sourceApp");
        if(!userInfo.get(RESULT).equals(SUCCESS)){
            return ErrorResponse.getSessionConnectedResponse(messageId,correlationToken,endpointId,
                    userInfo.get("type"),userInfo.get("message"));
        }

        try{
            String region = userInfo.get("region");
            String result = "";
            String uri = "/sdp/get";
            Long time = System.currentTimeMillis();
            System.out.println(uri);

            //iot请求参数，透传sdp数据
            JSONObject param = new JSONObject();
            param.put("sdp",sdp);
            param.put("accountTo",deviceName);
            param.put("accountFrom",userID);

            if(region.equals(SystemConstant.AS)){
                result = HttpUtil.requestPostUrlEncodedForm(SystemConstant.IOT_AS_URL + uri,param);
            }else if(region.equals(SystemConstant.EU)){
                result = HttpUtil.requestPostUrlEncodedForm(SystemConstant.IOT_EU_URL + uri,param);
            }else if(region.equals(SystemConstant.US)){
                result = HttpUtil.requestPostUrlEncodedForm(SystemConstant.IOT_US_URL + uri,param);
            }
            System.out.println(result);
            JSONObject resultJson = JSON.parseObject(result);

            if(resultJson != null && StringUtil.isNotNull((String)resultJson.get("sdpInfo"))){
                String responseSdp = (String)resultJson.get("sdpInfo");
                return AnswerGeneratedForSession.getAnswerGeneratedForSessionResponse(messageId,correlationToken,endpointId,responseSdp);
            }
        }catch(Exception e){
            System.out.println("push iot error" + e);
            e.printStackTrace();

        }

        return ErrorResponse.getSessionConnectedResponse(messageId,correlationToken,endpointId,
                INTERNAL_ERROR,"connect to device error");

    }

    /**
     *  根据SessionConnected 指令返回指令，请求的SessionConnected 指令格式为
     *  {
     *     "directive": {
     *         "header": {
     *           "namespace": "Alexa.RTCSessionController",
     *           "name": "SessionConnected",
     *           "messageId": "d1ba3aa7-bff7-4406-9425-f25f04ec8d68",
     *           "correlationToken": "dFMb0z+PgpgdDmluhJ1LddFvSqZ/jCc8ptlAKulUj90jSqg==",
     *           "payloadVersion": "3"
     *         },
     *         "endpoint": {
     *           "scope": {
     *               "type": "BearerToken",
     *               "token": "access-token-from-skill"
     *           },
     *           "endpointId": "appliance-001",
     *           "cookie": {
     *               "keys": "key/value pairs received during discovery",
     *             }
     *         },
     *         "payload": {
     *              "sessionId" : "session identifier"
     *          }
     *     }
     * }
     * @param request
     * @return
     */
    public JSONObject getSessionConnectedResponse(JSONObject request){

        return handleConnectAndDisconnectDirective(request,SessionConnected::getSessionConnectedResponse);
    }

    /**
     *  根据SessionDisconnected 指令返回，传入的SessionDisconnected 指令格式为
     *  {
     *     "directive": {
     *         "header": {
     *           "namespace": "Alexa.RTCSessionController",
     *           "name": "SessionDisconnected",
     *           "messageId": "d1ba3aa7-bff7-4406-9425-f25f04ec8d68",
     *           "correlationToken": "dFMb0z+PgpgdDmluhJ1LddFvSqZ/jCc8ptlAKulUj90jSqg==",
     *           "payloadVersion": "3"
     *         },
     *         "endpoint": {
     *           "scope": {
     *             "type": "BearerToken",
     *             "token": "access-token-from-skill"
     *           },
     *           "endpointId": "appliance-001",
     *           "cookie": {
     *               "keys": "key/value pairs received during discovery",
     *             }
     *         },
     *         "payload": {
     *             "sessionId" : "session identifier"
     *         }
     *     }
     * }
     * @param request
     * @return
     */
    public JSONObject getSessionDisconnectedResponse(JSONObject request){

        return handleConnectAndDisconnectDirective(request,SessionDisconnected::getSessionDisconnectedResponse);
    }

    private JSONObject handleConnectAndDisconnectDirective(JSONObject request,DirectiveGetter directiveGetter){

        String messageId = JsonUtil.findJsonValue(request,"directive","header","messageId");
        String correlationToken = JsonUtil.findJsonValue(request,"directive","header","correlationToken");
        String userToken = JsonUtil.findJsonValue(request,"directive","endpoint","scope","token");
        String endpointId = JsonUtil.findJsonValue(request,"directive","endpoint","endpointId");
        String sessionId = JsonUtil.findJsonValue(request,"directive","payload","sessionId");

        HashMap<String,String> userInfo = getAndValidUserToken(userToken);
        if(!userInfo.get(RESULT).equals(SUCCESS)){
            return ErrorResponse.getSessionConnectedResponse(messageId,correlationToken,endpointId,
                    userInfo.get("type"),userInfo.get("message"));
        }

        return directiveGetter.getDirectiveResponse(messageId,correlationToken,endpointId,sessionId);
    }

    public String getDeviceSign(String productKey, String deviceName, Long timestamp,String deviceSecret) {
        String value = productKey + "|" + deviceName + "|" + String.valueOf(timestamp);
        return HMAC_SHA1.genHMAC(value, deviceSecret);
    }

    private ArrayList<Map<String,String>> getDeviceInfo(HashMap<String,String> userInfo){
        ArrayList<Map<String,String>> deviceInfo = new ArrayList<Map<String,String>>();
        try{
            String region = userInfo.get("region");
            String userID = userInfo.get("userID");
            String result = "";
            if(region.equals(SystemConstant.AS)){
                result = HttpUtil.requestGet(SystemConstant.AS_URL + "/getDeviceForEchoshow?userID="+ userID);
            }else if(region.equals(SystemConstant.EU)){
                result = HttpUtil.requestGet(SystemConstant.EU_URL + "/getDeviceForEchoshow?userID="+ userID);
            }else if(region.equals(SystemConstant.US)){
                result = HttpUtil.requestGet(SystemConstant.US_URL + "/getDeviceForEchoshow?userID="+ userID);
            }
            JSONObject resultJson = JSON.parseObject(result);
            System.out.println(result);
            if(SUCCEED_RESULT_CODE.equals(resultJson.get(RESULT_CODE))){
                for(JSONObject ipc:(List<JSONObject>)resultJson.get(IPC)){
                    deviceInfo.add(JSONObject.toJavaObject(ipc, Map.class));
                }
            }
        }catch(Exception e){
            System.out.println("get device error" + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return deviceInfo;
    }

    private HashMap<String,String> getAndValidUserToken(String token){
        HashMap<String,String> userInfo = new HashMap<String, String>(5);
        try{
            String userToken = SymmetricEncryptionUtil.decrypt(symmetricKey,token);
            if(StringUtil.isNull(userToken)){
                userInfo.put("result",INVALID_AUTHORIZATION);
                userInfo.put("message","token is null");
                return userInfo;
            }else{
                String[] info = userToken.split("__");
                String userID = info[0];
                String region = info[1];
                String userDeviceSecret = info[2];
                String sourceApp = info[3];
                String time = info[4];
                System.out.println("<userID>"+userID+"<region>"+region+"<sourceApp>"+sourceApp);
                //超过两天的token认为无效
                if((new Date()).getTime()-Long.parseLong(time) > millisecondsOfOneDay*2){
                    System.out.println("two day expire");
                    userInfo.put("result",EXPIRED_ERROR);
                    userInfo.put("message","token has exceed 2 days and it is expired");
                    return userInfo;
                }
                userInfo.put("userID",userID);
                userInfo.put("region",region);
                userInfo.put("userDeviceSecret",userDeviceSecret);
                userInfo.put("sourceApp",sourceApp);
                userInfo.put("result",SUCCESS);
                return userInfo;

            }
        }catch(Exception e){
            System.out.println("valid error:" + e.getMessage());
            e.printStackTrace();
            userInfo.put("result",INTERNAL_ERROR);
            userInfo.put("message","internal error");
            return userInfo;

        }
    }

    public static void  main(String[] args){
        String uri = "/sdp/get?sdp=123&";
        Long time = System.currentTimeMillis();
        System.out.println(uri);

        //iot请求参数，透传sdp数据
        JSONObject param = new JSONObject();
        param.put("sdp","test");
        param.put("accountTo","12");
        param.put("accountFrom","22");
        try{
            String result = HttpUtil.requestPostUrlEncodedForm(SystemConstant.IOT_AS_URL + uri,param);
            System.out.println(result);
        }catch(Exception e){
            e.printStackTrace();
        }


    }
}
