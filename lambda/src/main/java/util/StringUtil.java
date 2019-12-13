package util;

import com.alibaba.fastjson.JSONObject;

import java.util.*;

public class StringUtil {
    public static boolean isNotNull(String str){
        if(str!=null && !str.equals("")){
            return true;
        }
        return false;
    }

    public static boolean isNull(String str){
        if(str==null || str.equals("")){
            return true;
        }
        return false;
    }

    public static String changeNull(String str) {
        if (str == null) {
            str = "";
        }
        return str;
    }
    /**
     * return UUID without "-"
     * @return
     */
    public static String getUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    /**
     * If str1 = null and str2 = null,return true
     * @param str1
     * @param str2
     * @return
     */
    public static boolean equalsIgnoreNull(String str1,String str2){
        if(str1 != null){
            return str1.equals(str2);
        }
        if(str2 != null){
            return str2.equals(str1);
        }
        return true;
    }
    /**
     * If str1 = null and str2 = null,return false
     * @param str1
     * @param str2
     * @return
     */
    public static boolean equals(String str1,String str2){
        if(str1 != null){
            return str1.equals(str2);
        }
        if(str2 != null){
            return str2.equals(str1);
        }
        return false;
    }
    /**
     * If ((str1 = null or str1 = "") and (str2 = null or str2 = "")),return true
     * @param str1
     * @param str2
     * @return
     */
    public static boolean trimEqualsNull(String str1,String str2){
        if(str1 != null && str1 != ""){
            return str1.equals(str2);
        }
        if(str2 != null && str2 != ""){
            return str2.equals(str1);
        }
        return true;
    }
    /**
     * JSONObject's return of a system error:{"resultCode":,"1002":requestID,"result":{"msg":"system error"}}
     * @param json
     * @param requestID
     * @return
     */
//    public static JSONObject getSystemError(JSONObject json,String requestID){
//        JsonResultVO jsonResultVO = new JsonResultVO();
//        jsonResultVO.setRequestID(requestID);
//        jsonResultVO.setResultCode(StatusCode.FAIL_CODE);
//        Map<String, String> map = new HashMap<String, String>();
//        map.put(StatusCode.MESSAGE, StatusCode.SYSTEM_ERROR);
//        jsonResultVO.setResult(map);
//        json = (JSONObject)JSONObject.toJSON(jsonResultVO);
//        return json;
//    }
    /**
     * replace timezone by firmID if (timezone == null) exists
     * @param timezone
     * @param firmID
     * @return
     */
    public static String getTimezone(String timezone,int firmID){
        if (StringUtil.isNull(timezone)){
            switch(firmID){
                case 1:
                    timezone = "CST08:00:00";
                    break;
                case 2:
                    timezone = "CST08:00:00";
                    break;
                case 3:
                    timezone = "CET01:00:00CEST02:00:00,M3.5.0,M10.5.0";
                    break;
                case 4:
                    timezone = "CET01:00:00CEST02:00:00,M3.5.0,M10.5.0";
                    break;
                case 5:
                    timezone = "CET01:00:00CEST02:00:00,M3.5.0,M10.5.0";
                    break;
                case 6:
                    timezone = "CST08:00:00";
                    break;
                case 7:
                    timezone = "EST-5:00:00CEDT-4:00:00,M3.2.0,M11.1.0"; //New_York
                    break;
                case 8:
                    timezone = "CST08:00:00";
                    break;
                default:
                    timezone = "CST08:00:00";
                    break;
            }
        }
        return timezone;
    }

    public static String getRegion(int sourceApp){
        switch(sourceApp){
            case 1:case 2:
                return "Asia/Shanghai";
            case 3:
                return "Europe/Warsaw";
            case 4:case 5:
                return "Europe/Paris";
            case 6:
                return "Asia/Shanghai";
            case 7:
                return "America/New_York";
            case 8:
                return "Asia/Shanghai";
            default:
                return "Asia/Shanghai";
        }
    }
    /**
     *
     * @param num
     * @return
     */
    public static String getRandomByNum(int num){
        return (num > 0 && num < 19) ? (((long)(Math.random()*9*(Math.pow(10, num-1))) + (long)Math.pow(10, num-1)) + "") : null;
    }

    public static String getArrayString(String[] alias){
        return Arrays.asList(alias).toString();
    }

    public static String getCapabilityProperties(Map deviceMap, String key){
        try{
            String capability = (String)deviceMap.get("capability");
            if(capability == null){
                return null;
            }
            JSONObject capabilityJson = JSONObject.parseObject(capability);
            if(capabilityJson == null){
                return null;
            }
            JSONObject capJson = capabilityJson.getJSONObject("caps");
            return capJson.getString(key);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        //System.out.println(UUID.randomUUID().toString());
        long start = System.currentTimeMillis();
        Set<String> setStr = new HashSet<String>();
        for(int i = 0;i < 100000;i++){
            String result = getRandomByNum(18);
            boolean flag = setStr.add(result);
            if(flag){
                System.out.println(flag + "-----" + result + "------" +result.length() + "---" + (i+1));
            }else{
                System.out.println(flag + "------------------------------------" + result + "------" +result.length() + "---" + (i+1));
            }

        }
        System.out.println(System.currentTimeMillis() - start);
    }
}
