package lambdaForWebrtc.api;

/**
 * @Interface MessageProcessor
 * @Description TODO
 * @Author huangqh
 * @CreateDate: 2019/4/24 18:50
 **/
public interface MessageProcessor {

    void processMessage(String sender, String message);
    void processError(String errorMessage);
    void processInfo(String infoMessage);
}
