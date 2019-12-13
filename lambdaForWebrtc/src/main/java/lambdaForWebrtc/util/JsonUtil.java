package lambdaForWebrtc.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * JSON 转换
 */
public final class JsonUtil {

    /**
     * 把Java对象转换成json字符串
     *
     * @param object 待转化为JSON字符串的Java对象
     * @return json 串 or null
     */
    public static String parseObjToJson(Object object) {
        String string = null;
        try {
            //string = JSON.toJSONString(object);
            string = JSONObject.toJSONString(object);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return string;
    }

    /**
     * 将Json字符串信息转换成对应的Java对象
     *
     * @param json json字符串对象
     * @param c    对应的类型
     */
    public static <T> T parseJsonToObj(String json, Class<T> c) {
        try {
            //两个都是可行的，起码我测试的时候是没问题的。
            //JSONObject jsonObject = JSONObject.parseObject(json);
            JSONObject jsonObject = JSON.parseObject(json);
            return JSON.toJavaObject(jsonObject, c);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     *  查找json中的数据
     * @param json
     * @param keys
     * @return
     */
    public static String findJsonValue(Map json, String...keys){
        try{
            for(int i=0;i<keys.length;i++){
                if(i<keys.length-1){
                    json = (Map)json.get(keys[i]);
                }else{
                    return (String)json.get(keys[i]);
                }

            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  设置json数据
     * @param json
     * @param value
     * @param keys
     * @return
     */
    public static String setJsonValue(Map json,Object value,String...keys){
        try{
            for(int i=0;i<keys.length;i++){
                if(i<keys.length-1){
                    json = (Map)json.get(keys[i]);
                }else{
                    return (String)json.put(keys[i],value);
                }

            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  向json中数据类型为数组的数据添加数据
     * @param json
     * @param value
     * @param keys
     * @return
     */
    public static boolean addJsonArrayValue(Map json,Object value,String...keys){
        try{
            for(int i=0;i<keys.length;i++){
                if(i<keys.length-1){
                    json = (Map)json.get(keys[i]);
                }else{
                    if(json.get(keys[i])!=null){
                        return ((JSONArray)json.get(keys[i])).add(value);
                    }else{
                        JSONArray jsonArray = new JSONArray();
                        jsonArray.add(value);
                    }

                }

            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
