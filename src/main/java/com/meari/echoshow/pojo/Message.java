package com.meari.echoshow.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message<T> implements Serializable {
    private static final long serialVersionUID = 8263472718471001421L;

    private String messageId;
    private T data;

    public Message() {
    }

    public Message(T data) {
        this.data = data;
    }

    public Message messageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void clean() {
        this.data = null;
    }

    @Override
    public String toString() {
        return dataStr() + "<messageId>" + messageId;
    }

    private String dataStr() {
        if (data == null) {
            return "";
        }
        if (data instanceof Object[]) {
            return Arrays.toString((Object[]) data);
        }
        return data.toString();
    }

    public static void main(String[] args) {
        String sdp = "v=0\n" +
                "o=- 3765414983 3765414983 IN IP4 0.0.0.0\n" +
                "s=a 2 z\n" +
                "c=IN IP4 0.0.0.0\n" +
                "t=0 0\n" +
                "a=group:BUNDLE audio0 video0\n" +
                "m=audio 1 UDP/TLS/RTP/SAVPF 96 0\n" +
                "a=candidate:1 1 UDP 2013266431 54.211.133.124 16101 typ host\n" +
                "a=candidate:3 1 TCP 1010827519 54.211.133.124 16257 typ host tcptype passive\n" +
                "a=candidate:2 1 TCP 1015021823 54.211.133.124 9 typ host tcptype active\n" +
                "a=candidate:2 2 TCP 1015021822 54.211.133.124 9 typ host tcptype active\n" +
                "a=candidate:1 2 UDP 2013266430 54.211.133.124 62237 typ host\n" +
                "a=candidate:3 2 TCP 1010827518 54.211.133.124 57434 typ host tcptype passive\n" +
                "a=setup:actpass\n" +
                "a=extmap:3 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time\n" +
                "a=rtpmap:96 opus/48000/2\n" +
                "a=rtcp:9 IN IP4 0.0.0.0\n" +
                "a=rtcp-mux\n" +
                "a=sendrecv\n" +
                "a=mid:audio0\n" +
                "a=ssrc:2876108513 cname:user2292916444@host-fcaa81d9\n" +
                "a=ice-ufrag:5g4D\n" +
                "a=ice-pwd:Sw5nqsX+vfMSdLdr3KMVKe\n" +
                "a=fingerprint:sha-256 FD:46:50:3C:F3:07:AF:2C:57:4D:CD:E0:1C:F1:B3:C2:55:F8:A3:84:41:12:2F:71:52:5C:F2:58:B1:D4:70:A9\n" +
                "m=video 1 UDP/TLS/RTP/SAVPF 99\n" +
                "a=candidate:1 1 UDP 2013266431 54.211.133.124 16101 typ host\n" +
                "a=candidate:3 1 TCP 1010827519 54.211.133.124 16257 typ host tcptype passive\n" +
                "a=candidate:2 2 TCP 1015021822 54.211.133.124 9 typ host tcptype active\n" +
                "a=candidate:1 2 UDP 2013266430 54.211.133.124 62237 typ host\n" +
                "a=candidate:2 1 TCP 1015021823 54.211.133.124 9 typ host tcptype active\n" +
                "a=candidate:3 2 TCP 1010827518 54.211.133.124 57434 typ host tcptype passive\n" +
                "b=AS:500\n" +
                "a=setup:actpass\n" +
                "a=extmap:3 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time\n" +
                "a=rtpmap:99 H264/90000\n" +
                "a=rtcp:9 IN IP4 0.0.0.0\n" +
                "a=rtcp-mux\n" +
                "a=sendrecv\n" +
                "a=mid:video0\n" +
                "a=rtcp-fb:99 nack\n" +
                "a=rtcp-fb:99 nack pli\n" +
                "a=rtcp-fb:99 ccm fir\n" +
                "a=ssrc:157315776 cname:user2292916444@host-fcaa81d9\n" +
                "a=ice-ufrag:5g4D\n" +
                "a=ice-pwd:Sw5nqsX+vfMSdLdr3KMVKe\n" +
                "a=fingerprint:sha-256 FD:46:50:3C:F3:07:AF:2C:57:4D:CD:E0:1C:F1:B3:C2:55:F8:A3:84:41:12:2F:71:52:5C:F2:58:B1:D4:70:A9";

        Sdp sdp1 = new Sdp(sdp);
        System.out.println(sdp1.getSdpInfo());

    }
}
