package com.meari.echoshow.control;

import com.alibaba.fastjson.JSONObject;
import com.meari.echoshow.await.AwaitMessageMode;
import com.meari.echoshow.await.MessageAwait;
import com.meari.echoshow.pojo.Sdp;
import com.meari.echoshow.sip.SipClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.sip.message.Request;

/**
 * @ClassName MessageController
 * @Description TODO
 * @Author huangqh
 * @CreateDate: 2019/4/25 15:50
 **/
@Controller
public class MessageController {

    private static Logger log = LoggerFactory.getLogger(MessageController.class);

    @Value("${sipPort}")
    private String sipPort;

    @Value("${sipUrl}")
    private String sipUrl;

    @PostConstruct
    public void init(){
        SipClient.buildSipClient(Integer.parseInt(sipPort),sipUrl);
    }

    @PostMapping("/sdp/get")
    @ResponseBody
    Mono<JSONObject> sdpGet(
            @RequestParam() String sdp,
            @RequestParam() String accountTo,
            @RequestParam() String accountFrom
            ){
        log.info("<accountTo>{}<accountFrom>{}<sdp>{}",accountTo,accountFrom,sdp);
        Sdp changeSdp = new Sdp(sdp);

        log.info("<changeSdp>{}",changeSdp.getSdpInfo());

        SipClient.getInstance().sendRegister(accountFrom);
        String requestId = SipClient.getInstance().sendMessage(accountFrom,accountTo,changeSdp.getSdpInfo(),Request.INVITE);

        return MessageAwait.await(new AwaitMessageMode(null,null,10,requestId))
                .map(message -> {
                    String ramdom = String.valueOf(Math.round(Math.random()*10));
                    String sdpString = (String)message.getData();
                    sdpString = sdpString.replace("t=0 0","t=0 0\r\na=group:BUNDLE audio" + ramdom + " video" + ramdom);
                    sdpString = sdpString.replaceFirst("opus/48000/2","opus/48000/2\r\na=mid:audio" + ramdom);
                    sdpString = sdpString.replace("H264/90000","H264/90000\r\na=rtcp-fb:99 nack\r\na=rtcp-fb:99 nack pli\r\na=rtcp-fb:99 ccm fir\r\na=mid:video" + ramdom);
                    JSONObject result = new JSONObject();
                    result.put("sdpInfo",sdpString);
                    return result;
                }).defaultIfEmpty(new JSONObject());

    }

    @PostMapping("/sip/bye")
    @ResponseBody
    Mono<JSONObject> sipBye(
            @RequestParam() String accountTo,
            @RequestParam() String accountFrom
    ){
        log.info("<accountTo>{}<accountFrom>{}",accountTo,accountFrom);

        SipClient.getInstance().sendRegister(accountFrom);
        String requestId = SipClient.getInstance().sendMessage(accountFrom,accountTo,"bye",Request.BYE);

        return MessageAwait.await(new AwaitMessageMode(null,null,5,requestId))
                .map(message -> {
                    String data = (String)message.getData();
                    System.out.println(data);
                    JSONObject result = new JSONObject();
                    result.put("resultCode","1001");
                    return result;
                }).defaultIfEmpty(new JSONObject());

    }
}
