package com.meari.echoshow.pojo;

import com.meari.echoshow.util.StringUtil;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName Sdp
 * @Description TODO
 * @Author huangqh
 * @CreateDate: 2019/4/28 9:39
 **/
public class Sdp {
    /**
     * sdp信息
     */
    private String sdpInfo;
    /**
     * candidate属性中的ip
     */
    private String ip;
    /**
     * candidate属性中的优先度和端口号的映射，用于获取优先度最高的candidate的port
     */
    private HashMap<String,String> priorityPortMap = new HashMap<>(4);
    private String port;
    /**
     * udp的candidate的正则匹配
     */
    private static String udpCandidateRex = "a=candidate.*?UDP.*?\r\n";
    private static Pattern udpPattern = Pattern.compile(udpCandidateRex);

    private static String audioRex = "m=audio.*?\r\n";
    private static Pattern audioPattern = Pattern.compile(audioRex);
    private static String ipRex = "c=IN IP4.*?\r\n";
    private static Pattern ipPattern = Pattern.compile(ipRex);
    private static String vedioRex = "m=video.*?\r\n";
    private static Pattern vedioPattern = Pattern.compile(vedioRex);

    public Sdp(String sdp){
        this.sdpInfo = sdp;

        //通过candidate属性获取 ip和端口号
        Matcher m = udpPattern.matcher(sdpInfo);
        ip = "0.0.0.0";
        while(m.find()){
            //candidate示例   a=candidate:1 2 UDP 2013266430 18.206.140.120 36221 typ host
            String[] info = m.group().split("\\ ");
            if(info != null && info.length >= 6){
                System.out.println("<info>" + info[5]);
                priorityPortMap.put(info[3],info[5]);
                ip = info[4];
            }
        }

        int max = 0;
        for(String key:priorityPortMap.keySet()){
            if(Integer.parseInt(key) > max){
                max = Integer.parseInt(key);
            }
        }

        port = "1";
        if(StringUtil.isNotNull(priorityPortMap.get(String.valueOf(max)))){
            port = priorityPortMap.get(String.valueOf(max));
        }

        String oldIpAttr = "";
        Matcher ipMatcher = ipPattern.matcher(sdpInfo);
        if(ipMatcher.find()){
            //ip的示例 c=IN IP4 18.206.140.120
            String[] info = ipMatcher.group().split("\\ ");
            if(info != null && info.length >= 3){
                System.out.println("<oldIp>" + info[2]);
                String oldIp = info[2];
                String oldInfo = "c=IN IP4 " + oldIp;
                String newInfo = "c=IN IP4 " + ip + "\n";
                sdpInfo = sdpInfo.replace(oldInfo,newInfo);
            }
        }

        //获取audio的m属性并替换端口号
        Matcher audioMathcer = audioPattern.matcher(sdpInfo);
        while(audioMathcer.find()){
            //m属性示例   m=audio 1 UDP/TLS/RTP/SAVPF 96 0
            String[] info = audioMathcer.group().split("\\ ");
            if(info != null && info.length >= 2){
                System.out.println("<candatePort>" + info[1]);
                String candatePort = info[1];
                String oldAudio = "m=audio " + candatePort;
                String newAudio = "m=audio " + port;
                sdpInfo = sdpInfo.replace(oldAudio,newAudio);
            }
        }

        //获取audio的m属性并替换端口号
        Matcher vedioMathcer = vedioPattern.matcher(sdpInfo);
        while(vedioMathcer.find()){
            //m属性示例   m=audio 1 UDP/TLS/RTP/SAVPF 96 0
            String[] info = vedioMathcer.group().split("\\ ");
            if(info != null && info.length >= 2){
                String candatePort = info[1];
                String oldVedio = "m=video " + candatePort;
                String newVedio = "m=video " + port;
                sdpInfo = sdpInfo.replace(oldVedio,newVedio);
            }
        }
    }

    public String getSdpInfo() {
        return sdpInfo;
    }

    public void setSdpInfo(String sdpInfo) {
        this.sdpInfo = sdpInfo;
    }
}
