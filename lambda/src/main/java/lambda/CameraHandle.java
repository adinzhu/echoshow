package lambda;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context;
import constant.SystemConstant;
import pojo.*;
import util.*;

import java.time.Instant;
import java.util.*;

/**
 * @author huangqh
 */
public class CameraHandle  implements  RequestHandler<Request, Object> {

    private String symmetricKey = "FCEEB1B960E91A5AD1EBC29C6B03000B";
    private String DISCOVER = "Discover";
    private static String INITIALIZE_CAMERA_STREAMS = "InitializeCameraStreams";
    private static String SUCCEED_RESULT_CODE = "1001";
    private static String RESULT_CODE = "resultCode";
    private static String MEDIA_SERVER_URL_FROM_BALANCE = "url";
    private String mediaUrlCache = null;
    private String expireSeconds = "86400";
    private String deviceTypes[] = {"doorbell","fourthGeneration","ipc","snap"};

    public Object handleRequest(Request request, Context context) {
        System.out.println(GsonUtil.GsonString(request));

        String directiveType = request.getDirective().getHeader().getName();
        Response response = new Response(request);


        if( DISCOVER.equals(directiveType)){
            this.discoverCamera(request,response);
        }else if(INITIALIZE_CAMERA_STREAMS.equals(directiveType)){
            this.initCamera(request,response);
        }

        //interface不能作为字段，需要用别的字段，然后这里改回去
        String responseJson = JsonUtil.parseObjToJson(response);
        responseJson = responseJson.replaceAll("interface_meari_special","interface");
        JSONObject jsonObject = JSONObject.parseObject(responseJson);
        System.out.println(jsonObject);

        return jsonObject;

    }

    private void discoverCamera(Request request,Response response){

        String userToken = request.getDirective().getPayload().getScope().getToken();
        UserInfoDTO userInfo = getAndValidUserToken(userToken);
        ArrayList<Map<String,String>> deviceInfo;
        //userInfo为空，表示认证异常,discover指令返回空的设备
        if(userInfo == null){
            deviceInfo = new ArrayList<Map<String,String>>();
        }else{
            deviceInfo = this.getDeviceInfo(userInfo,userToken);
        }

        response.getEvent().getHeader().setName("Discover.Response");
        Payload payload = new Payload();
        response.getEvent().setPayload(payload);
        ArrayList<Endpoint> endpoints = new ArrayList<Endpoint>();
        payload.setEndpoints(endpoints);

        for(Map<String,String> application:deviceInfo) {

            Endpoint endpoint = new Endpoint();
            endpoint.setManufacturerName("meari");
            endpoint.setModelName("the model name of the endpoint,for testing server");
            endpoint.setFriendlyName(application.get("deviceName"));
            endpoint.setEndpointId(application.get("snNum"));
            endpoint.setDescription("meari camera for testing server");
            ArrayList<String> displayCategories = new ArrayList<String>();
            displayCategories.add("CAMERA");
            endpoint.setDisplayCategories(displayCategories);
            //endpoint.getCookie().put("key","value");

            Capabilities capabilities = new Capabilities();
            capabilities.setType("AlexaInterface");
            capabilities.setInterface_meari_special("Alexa.CameraStreamController");
            capabilities.setVersion("3");

            CameraStreamConfigurations cameraStreamConfigurations = new CameraStreamConfigurations();
            cameraStreamConfigurations.getProtocols().add("RTSP");
            cameraStreamConfigurations.getAuthorizationTypes().add("NONE");
            cameraStreamConfigurations.getVideoCodecs().add("H264");
            cameraStreamConfigurations.getVideoCodecs().add("MPEG2");
            cameraStreamConfigurations.getAudioCodecs().add("G711");
            cameraStreamConfigurations.getAudioCodecs().add("AAC");
            cameraStreamConfigurations.getResolutions().add(new Resolution(1920, 1080));
            cameraStreamConfigurations.getResolutions().add(new Resolution(1280, 720));

            capabilities.getCameraStreamConfigurations().add(cameraStreamConfigurations);
            ArrayList<Capabilities> allCapabilities = new ArrayList<Capabilities>();
            allCapabilities.add(capabilities);
            endpoint.setCapabilities(allCapabilities);

            endpoints.add(endpoint);
        }


    }

    private void initCamera(Request request,Response response){
        String userToken = request.getDirective().getEndpoint().getScope().getToken();
        UserInfoDTO userInfo = getAndValidUserToken(userToken);
        //userInfo中的result属性不等于success，表示认证异常，根据错误类型返回errorResponse
        if(userInfo == null){
            generateErrorResponse(request,response,"error");
            return;
        }
        //这里的endpointId就是snNum
        String applicationId = request.getDirective().getEndpoint().getEndpointId();
        response.getEvent().getHeader().setName("Response");
        response.getEvent().setEndpoint(new Endpoint());
        CameraStreams cameraStreams = new CameraStreams();

        String rtspUrl = this.getRtspUrl(userInfo,applicationId);
        if(StringUtil.isNull(rtspUrl)){
            return;
        }
        cameraStreams.setUri(rtspUrl);
        //cameraStreams.setExpirationTime(Instant.now().plusSeconds(120).toString());
        cameraStreams.setExpirationTime("2019-12-20T08:02:12.828Z");
        cameraStreams.setIdleTimeoutSeconds(15);
        cameraStreams.setProtocol("RTSP");
        //cameraStreams.setResolution(request.getDirective().getPayload().getCameraStreams().get(0).getResolution());
        Resolution resolution = new Resolution();
        resolution.setHeight(1080);
        resolution.setWidth(1920);
        cameraStreams.setResolution(resolution);
        cameraStreams.setAuthorizationType("NONE");
        cameraStreams.setVideoCodec("H264");
        cameraStreams.setAudioCodec("AAC");

        response.getEvent().setPayload(new Payload());
        response.getEvent().getPayload().setCameraStreams(new ArrayList<CameraStreams>());
        response.getEvent().getPayload().getCameraStreams().add(cameraStreams);
        response.getEvent().getPayload().setImageUri("https://pis.meari.com.cn/img/logo.32b4ce85.png");
        response.getEvent().getEndpoint().setEndpointId(request.getDirective().getEndpoint().getEndpointId());
    }

    private ArrayList<Map<String,String>> getDeviceInfo(UserInfoDTO userInfo,String userToken){
        ArrayList<Map<String,String>> deviceInfo = new ArrayList<Map<String,String>>();
        try{
            String region = userInfo.getRegion();
            String uri = "/api/thirdDevice/getDeviceForThirdDevice?userID="+ userInfo.getUserID();
            String result = getCurlResult(region,uri);

            System.out.println(result);
            JSONObject resultJson = JSON.parseObject(result);
            if(SUCCEED_RESULT_CODE.equals(resultJson.get(RESULT_CODE))){
                for(String type:deviceTypes){
                    for(Map device:(List<Map>)resultJson.get(type)){
                        String ecs = StringUtil.getCapabilityProperties(device,"ecs");
                        if(StringUtil.isNotNull(ecs) && ecs.equals("1")){
                            deviceInfo.add(device);
                        }

                    }
                }
            }
        }catch(Exception e){
            System.out.println("get device error" + e.getMessage());
            return null;
        }
        return deviceInfo;
    }

    private String getRtspUrl(UserInfoDTO userInfo,String licenseID){
        try{
            String region = userInfo.getRegion();
            String uri = "/api/thirdDevice/echoshow/getRtspUrl?userID=" + userInfo.getUserID() + "&licenseID=" + licenseID;
            String result = getCurlResult(region,uri);
            System.out.println(result);
            JSONObject json = (JSONObject)JSONObject.parse(result);
            String url = json.getString(MEDIA_SERVER_URL_FROM_BALANCE);
            if(StringUtil.isNotNull(url)){
                //缓存最后一次获取到的流媒体服务器地址，当均衡负载服务器挂了或者其他原因导致没有返回url时，可以使用此url勉强试一试
                mediaUrlCache = url;
                return url;
            }else{
                return mediaUrlCache;
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            return mediaUrlCache;
        }

    }

    private UserInfoDTO getAndValidUserToken(String token){
        try{
            String userToken = SymmetricEncryptionUtil.decrypt(symmetricKey,token);
            if(StringUtil.isNull(userToken)){
                System.out.println("<getUserDataFromTokenError>token is null");
                return null;
            }else{
                String[] info = userToken.split("__");
                String userID = info[0];
                String region = info[1];
                String userDeviceSecret = info[2];
                String sourceApp = info[3];
                String time = info[4];
                System.out.println("<userID>"+userID+"<region>"+region+"<sourceApp>"+sourceApp);
                //超过过期时间的token认为无效
                long expireMilliSecond = Math.round(Long.parseLong(this.expireSeconds)*1000*1.1);
                if((new Date()).getTime()-Long.parseLong(time) > expireMilliSecond){
                    System.out.println("<getUserDataFromTokenError>token has expired");
                    return null;
                }
                UserInfoDTO userInfoDTO = new UserInfoDTO();
                userInfoDTO.setUserID(userID);
                userInfoDTO.setRegion(region);
                userInfoDTO.setSourceApp(sourceApp);
                userInfoDTO.setUserDeviceSecret(userDeviceSecret);
                return userInfoDTO;

            }
        }catch(Exception e){
            System.out.println("<getUserDataFromTokenError>internal error<error>" + e.getLocalizedMessage());
            return null;

        }
    }

    private String getCurlResult(String region,String uri) throws Exception {
        if (StringUtil.isNull(region)) {
            return "";
        } else if (region.equals(SystemConstant.AS)) {
            return HttpUtil.requestGet(SystemConstant.AS_URL + uri);
        } else if (region.equals(SystemConstant.EU)) {
            return HttpUtil.requestGet(SystemConstant.EU_URL + uri);
        } else if (region.equals(SystemConstant.US)) {
            return HttpUtil.requestGet(SystemConstant.US_URL + uri);
        }
        return "";
    }

    private void generateErrorResponse(Request request,Response response,String errorType){
        response.getEvent().getHeader().setName("ErrorResponse");
        response.getEvent().getHeader().setNamespace("Alexa");
        Payload payload = new Payload();
        response.getEvent().setPayload(payload);
        payload.setType(errorType);
        payload.setMessage("error type is" + errorType);
        Endpoint endpoint = new Endpoint();
        response.getEvent().setEndpoint(endpoint);
        endpoint.setEndpointId(request.getDirective().getEndpoint().getEndpointId());
        Scope scope = new Scope();
        endpoint.setScope(scope);
        scope.setType("BearerToken");
        scope.setToken(request.getDirective().getEndpoint().getScope().getToken());

    }

    public static void  main(String[] args){
        //CameraHandle cameraHandle = new CameraHandle();
        //String s = cameraHandle.handleRequest("{\"directive\":{\"header\":{\"namespace\":\"Alexa.CameraStreamController\",\"name\":\"InitializeCameraStreams\",\"payloadVersion\":\"3\",\"messageId\":\"1bd5d003-31b9-476f-ad03-71d471922820\",\"correlationToken\":\"dFMb0z+PgpgdDmluhJ1LddFvSqZ/jCc8ptlAKulUj90jSqg\\u003d\\u003d\"},\"endpoint\":{\"endpointId\":\"appliance-001\",\"scope\":{\"type\":\"BearerToken\",\"token\":\"access-token-from-skill\"},\"cookie\":{}},\"payload\":{\"cameraStreams\":[{\"protocol\":\"RTSP\",\"resolution\":{\"width\":1920,\"height\":1080},\"authorizationType\":\"BASIC\",\"videoCodec\":\"H264\",\"audioCodec\":\"AAC\"},{\"protocol\":\"RTSP\",\"resolution\":{\"width\":1280,\"height\":720},\"authorizationType\":\"NONE\",\"videoCodec\":\"MPEG2\",\"audioCodec\":\"G711\"}]}}}",null).toString();
        //System.out.print(s);
        /*String s = "{\"event\":{\"endpoint\":{\"capabilities\":[{\"cameraStreamConfigurations\":[{\"audioCodec\":[\"G711\",\"AAC\"],\"authorizationType\":[\"NONE\"],\"protocol\":[\"RTSP\"],\"resolution\":[{\"height\":1080,\"width\":1920},{\"height\":720,\"width\":1280}],\"videoCodec\":[\"H264\",\"MPEG2\"]}],\"interface_meari_special\":\"Alexa.CameraStreamController\",\"type\":\"AlexaInterface\",\"version\":\"3\"}],\"cookie\":{},\"description\":\"a description that is shown to the customer\",\"displayCategories\":[\"CAMERA\"],\"endpointId\":\"uniqueIdOfCameraEndpoint\",\"friendlyName\":\"camera\",\"manufacturerName\":\"meari\",\"modelName\":\"the model name of the endpoint\"},\"header\":{\"messageId\":\"0a8742de-5a1b-47f2-9ec4-06f364569051\",\"name\":\"Discover.Response\",\"namespace\":\"Alexa.Discovery\",\"payloadVersion\":\"3\"}}}";
        s=s.replaceAll("interface_meari_special","interface");*/

        /*String s = "{" +
                "  \"directive\": {" +
                "    \"header\": {" +
                "      \"namespace\": \"Alexa.Discovery\"," +
                "      \"name\": \"Discover\"," +
                "      \"payloadVersion\": \"3\"," +
                "      \"messageId\": \"36f492a1-37b9-4335-bd0b-a4646facfddc\"" +
                "    }," +
                "    \"payload\": {" +
                "      \"scope\": {" +
                "        \"type\": \"BearerToken\"," +
                "        \"token\": \"MTJaxwyuFFawcA9AXIO6GSHRyoAK1kVKnEa89j9KNsydoy23aeUYNAZANDZzoUIc\"" +
                "      }" +
                "    }" +
                "  }" +
                "}";*/
        /*String s = "{\n" +
                "    \"directive\": {\n" +
                "        \"header\": {\n" +
                "            \"namespace\": \"Alexa.CameraStreamController\",\n" +
                "            \"name\": \"InitializeCameraStreams\",\n" +
                "            \"payloadVersion\": \"3\",\n" +
                "            \"messageId\": \"a522d572-a453-4d1a-8627-0c15844be2e8\",\n" +
                "            \"correlationToken\": \"AAAAAAAAAABFydkQ8b/VE44WwUu7hHBcDAIAAAAAAAC878dWGynuSwSPJaI3y7ReMpobUjnI61c/xPTqAxbf0NUHn3xZOI0vccf8KDoo5d9nylRk+BnWvrqOMlQXn/j/yk1QMCYIoJPgEp5ayyGbgzxWgxaQGhERecU4m/6WoCGyFayZWkos9E4qopdk/42DMitcGPlHQnaTthJ2FPzS15GbzHEM+IxG8LrZQ5Vij3VQVMVyNFlouXUACc2r7bQes5TM/TnZW0vu119O6Wrx8gqnKvVqsKK5rqZdc7pktV9XXwkgGtrYH0nvV0qyaHb56t5Va+ucJ2L9Rm9v9Sv3asnNtvIbBsoVD9iPgdnghRYP9huKewoJdlY2bj0zzuzR/zqd63ChAZGIDyzoeezKaagH4aSCQcBE6vtFkTqMzCCkMLIDnBWxzVhrskz+R0IeRY6aiRpWlqiXAL+Jm7e/GJ3Phaakq2/xvcbiL/hqzPeW1LTd88YIWARBYQgtI3DTiSgeDWb7P1rJPLDOVooqiZG7yJVzVmars41nxMH2YzrnGrWkXaMQc7sv5LKoidybpGjiz+NMTo/W0eZNGvqaLECmVXlwhZVcq1JNK+MKUaEbG+Ab+gXUN91zorVTGWNFJmmZeE7ur+bCs/qdq7Vzdc/GcS2DG+uWlOSQ9CUtOiWy8yCdV6+s0sPolQlKHCywEy3sr7sAp9ZQoFr8CSgQ/U0tP2gczmowFy/hOw==\"\n" +
                "        },\n" +
                "        \"endpoint\": {\n" +
                "            \"endpointId\": \"a\",\n" +
                "            \"scope\": {\n" +
                "                \"type\": \"BearerToken\",\n" +
                "                \"token\": \"MTJaxwyuFFawcA9AXIO6GSHRyoAK1kVKnEa89j9KNsydoy23aeUYNAZANDZzoUIc\"\n" +
                "            },\n" +
                "            \"cookie\": {}\n" +
                "        },\n" +
                "        \"payload\": {\n" +
                "            \"cameraStreams\": [\n" +
                "                {\n" +
                "                    \"protocol\": \"RTSP\",\n" +
                "                    \"authorizationType\": \"NONE\",\n" +
                "                    \"videoCodec\": \"H264\",\n" +
                "                    \"audioCodec\": \"G711\",\n" +
                "                    \"resolution\": {\n" +
                "                        \"width\": 1280,\n" +
                "                        \"height\": 720\n" +
                "                    }\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    }\n" +
                "}";*/
        //Request request = JsonUtil.parseJsonToObj(s,Request.class);
        //cameraHandle.handleRequest(request,null);
        /*HttpUtil httpUtil = new HttpUtil();
        try{
            String result = "";
            result = httpUtil.requestPost("http://192.168.1.21:10007/getMediaServerUrl",null,null);
            JSONObject json = (JSONObject)JSONObject.parse(result);
            System.out.println(json.get("ip"));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }*/
    }
}
