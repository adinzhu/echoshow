package com.meari.echoshow.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meari.echoshow.dao.EchoshowRedis;
import com.meari.echoshow.pojo.Token;
import com.meari.echoshow.util.Base64;
import com.meari.echoshow.util.StringUtil;
import com.meari.echoshow.util.SymmetricEncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class TokenCache {
    /**
     * redis中存储的值有两个，分别是access_token和refresh_token，access_token是用户名加上 __ 以及System.currentTimeMillis，
     * 然后通过对称加密生成的字符串，refresh_token是生成的用于刷新token的uuid
     */
    private static Logger log = LoggerFactory.getLogger(TokenCache.class);
    @Value("${symmetricKey}")
    private String symmetricKey;

    private static final String SPILT_SIGN = "__";

    @Autowired
    private EchoshowRedis redis;

    public Token valid(String token, String refreshToken){
        //token不为空表示是第一次关联账号，是传入code获取refreshToken的
        if(token != null){
            log.info("<getRefreshToken><encryptedToken>"+ token);
            token = new String(Base64.decode(token));
            token = SymmetricEncryptionUtil.decrypt(symmetricKey,token);
            log.info("<getRefreshToken>" + token + ":" + refreshToken);
            if(token == null){
                return null;
            }
            String[] tokenArray = token.split(SPILT_SIGN);
            Token tokenDTO = Token.toToken(redis.getHashMap(tokenArray[0]));
            if(tokenDTO != null && token.equals(tokenDTO.getAccess_token())){
                return new Token(SymmetricEncryptionUtil.encrypt(symmetricKey,token),tokenDTO.getRefresh_token());
            }else{
                return null;
            }
        }else if(refreshToken != null){
            //token为空refreshToken不为空，表示客户端的token过期了，通过refreshToken来获取新的token
            log.info(token + ":" + refreshToken);
            String[] refreshArray = refreshToken.split(SPILT_SIGN);
            String userId = refreshArray[0];
            if(StringUtil.isNull(userId)){
                return null;
            }
            Token tokenDTO = Token.toToken(redis.getHashMap(userId));
            if(tokenDTO!=null && refreshToken.equals(tokenDTO.getRefresh_token())){
                redis.delete(userId);
                String newRefreshToken = userId + SPILT_SIGN + UUID.randomUUID().toString().replace("-","");
                String[] tokenArray = tokenDTO.getAccess_token().split(SPILT_SIGN);
                String newToken = tokenArray[0] + SPILT_SIGN + tokenArray[1] + SPILT_SIGN +tokenArray[2] + SPILT_SIGN+tokenArray[3] + SPILT_SIGN + System.currentTimeMillis();
                redis.setHashMap(userId,Token.toTokenMap(newToken,newRefreshToken));
                log.info("<refreshToken>" + newToken + ":" + newRefreshToken);
                return new Token(SymmetricEncryptionUtil.encrypt(symmetricKey,newToken),newRefreshToken);
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

    public String generateToken(JSONObject userInfo, String region){
        String token = userInfo.getString("userID") + SPILT_SIGN + region + SPILT_SIGN + userInfo.getString("deviceSecret") + SPILT_SIGN +
                userInfo.getString("sourceApp") + SPILT_SIGN + System.currentTimeMillis();
        String refreshToken = userInfo.getString("userID") + SPILT_SIGN +UUID.randomUUID().toString().replace("-","");
        redis.setHashMap(userInfo.getString("userID"),Token.toTokenMap(token,refreshToken));
        log.info("<generateToken>" + token + ":" + refreshToken);
        String encryptedToken = SymmetricEncryptionUtil.encrypt(symmetricKey,token);
        encryptedToken = Base64.encode(encryptedToken.getBytes());
        log.info("<generateToken><encryptedToken>"+ encryptedToken);
        return encryptedToken ;
    }
}
