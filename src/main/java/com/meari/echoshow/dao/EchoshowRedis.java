package com.meari.echoshow.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AlertImage redis操作
 *
 * @author fudong
 */
@Component
public class EchoshowRedis {
    private static Logger logger = LoggerFactory.getLogger(EchoshowRedis.class);
    private static final String NIL = "nil";

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Autowired(required = false)
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        RedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        this.redisTemplate = redisTemplate;
    }

    public void setHashMap(String key, Map map) {
        redisTemplate.opsForHash().putAll(key,map);
    }

    public void setHashMap(String key, Map map, long timeout) {
        redisTemplate.opsForHash().putAll(key,map);
        redisTemplate.expire(key,timeout,TimeUnit.SECONDS);
    }

    public Map<String,String> getHashMap(String key){
        BoundHashOperations<String, String, String> ops = redisTemplate.opsForHash().getOperations().boundHashOps(key);
        return ops.entries();
    }

    public void delete(String key){
        try{
            redisTemplate.delete(key);
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }


}
