package lambdaForWebrtc.api;

import com.alibaba.fastjson.JSONObject;

/**
 * @Interface DirectiveGetter
 * @Description TODO
 * @Author huangqh
 * @CreateDate: 2018/12/12 15:56
 **/
@FunctionalInterface
public interface DirectiveGetter {
    JSONObject getDirectiveResponse(String messageId,String correlationToken,String endpointId,String sessionId);
}
