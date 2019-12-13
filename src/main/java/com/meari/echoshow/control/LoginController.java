package com.meari.echoshow.control;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meari.echoshow.Constant.SystemConstant;
import com.meari.echoshow.cache.TokenCache;
import com.meari.echoshow.pojo.Token;
import com.meari.echoshow.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


@Controller
public class LoginController {
    private static Logger log = LoggerFactory.getLogger(LoginController.class);
    private static String mobileAgents[] = { "Android", "iPhone", "SymbianOS", "Windows Phone", "iPad","iPod"};
    @Resource
    private TokenCache cache;
    @Value("${clientSecret}")
    private String clientSecret;
    @Value("${expireSeconds}")
    private String expireSeconds;
    @Value("${euUrl}")
    private String euUrl;
    @Value("${asUrl}")
    private String asUrl;
    @Value("${usUrl}")
    private String usUrl;

    @RequestMapping(path = "/login",method = GET)
    public String login(
            String state,
            String client_id,
            String redirect_uri,
            String response_type,
            String source_app,
            @RequestHeader("user-agent") String agent,
            HttpSession session
    ){

        log.info("<loginGet><state>{}<client_id>{}<redirect_uri>{}<agent>{}<response_type>{}<source_app>{}",state,client_id,redirect_uri,agent,response_type,source_app);
        session.setAttribute("state",state);
        session.setAttribute("client_id",client_id);
        session.setAttribute("redirect_uri",redirect_uri);
        session.setAttribute("redirect_uri",redirect_uri);
        session.setAttribute("source_app",source_app);
        agent = agent==null?"":agent;
        for(String mobileAgent:mobileAgents){
            if(agent.contains(mobileAgent)){
                return "loginForMobile"+source_app;
            }
        }
        return "login"+source_app;
    }

    @ResponseBody
    @RequestMapping(path = "/login",method = POST)
    public String getToken(
            @RequestParam("name") String name,
            @RequestParam("country") String country,
            @RequestParam("password") String password,
            HttpServletResponse response,
            HttpSession session
    ){

        String source_app = (String)session.getAttribute("source_app");
        response.setStatus(200);
        String region = CountryRegionMap.getRegionByCountry(country);
        log.info("<name>{}<country>{}<source_app>{}<region>{}" , name , country ,source_app,region);
        if(StringUtil.isNull(name)||StringUtil.isNull(country)||StringUtil.isNull(source_app)||StringUtil.isNull(region)){
            response.setStatus(500);
            return null;
        }
        String realPassword = "";
        try{
            //非对称解密前端发过来的密码，然后用对称加密密码传到主业务系统，因为数据库中存的密码是对称加密的
            realPassword = RSA.decrypt(Base64.decode(password),RSA.getDefaultPrivateKey());
            realPassword = DesUtils.encode(realPassword);
        }catch (Exception e){
            log.info("decrypt error:" + e.getMessage());
            response.setStatus(500);
            return null;
        }
        HttpUtil httpUtil = new HttpUtil();
        String result = "";
        JSONObject userInfo = new JSONObject() ;
        boolean authenticateSuccees = false;

        try{
            if(region.equals(SystemConstant.AS)){
                result = httpUtil.requestGet(String.format("%s/checkAccountLink?userAccount=%s&password=%s&sourceApp=%s",asUrl,name,realPassword,source_app));
            }else if(region.equals(SystemConstant.EU)){
                result = httpUtil.requestGet(String.format("%s/checkAccountLink?userAccount=%s&password=%s&sourceApp=%s",euUrl,name,realPassword,source_app));
            }else if(region.equals(SystemConstant.US)){
                result = httpUtil.requestGet(String.format("%s/checkAccountLink?userAccount=%s&password=%s&sourceApp=%s",usUrl,name,realPassword,source_app));
            }
            log.info("<checkAccountLink>result: {}",result);
            JSONObject resultJson = JSON.parseObject(result);

            if(resultJson.get("resultCode").equals("1001")){
                authenticateSuccees = true;
                userInfo = (JSONObject)resultJson.get("content");
            }
        }catch(Exception e){
            log.info("send message error:" + e.getMessage());
            response.setStatus(500);
            return null;
        }

        if(authenticateSuccees){
            String params = "?state=";
            params += session.getAttribute("state");
            /*params += "&client_id=";
            params += session.getAttribute("client_id");
            params += "&client_secret=" + clientSecret;*/
            params += "&code=" + cache.generateToken(userInfo,region);
            log.info("user " + name + "login successfully, redirect param is: " + params);
            return session.getAttribute("redirect_uri") + params;
        }else{
            log.info("login error");
            return null;
        }

    }

    @ResponseBody
    @RequestMapping(path = "/authorize",method = {POST,GET},produces="application/json;charset=UTF-8")
    public String authorize(
            @RequestParam(value="code",required = false) String token,
            @RequestParam(value="refresh_token",required = false) String refreshToken
    ){

        log.info("<code>{}<refresh_token>{}",token,refreshToken);
        if(null != token || null != refreshToken){
            Token validToken = cache.valid(token,refreshToken);
            if(validToken!=null){
                Map<String,String> map=new HashMap<String, String>();
                map.put("access_token", validToken.getAccess_token());
                map.put("token_type", "Bearer");
                map.put("expires_in", expireSeconds);
                map.put("refresh_token", validToken.getRefresh_token());
                String jsonString = JSON.toJSONString(map);
                return  jsonString;
            }else{
                return "";
            }

            //return "{\"token_type\":\"Bearer\",\"client_id\":\"meariCamera\",\"client_secret\":\"1a2b3c4d5e6f7g\",\"expires_in\":3600,\"refresh_token\":\"123456\",\"access_token\":\"" + request.getParameter("code") + "\"}";
        }
        return "";
    }

    @RequestMapping(path = "/health",method = GET)
    public void health(HttpServletResponse response){

        response.setStatus(200);
    }
}
