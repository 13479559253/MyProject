package com.example.demo.util;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }
    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value,timeout, TimeUnit.SECONDS);
    }
    public String get(String key){
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? null : value.toString();
    }
    public void remove(String key){
        redisTemplate.delete(key);
    }
    public Boolean setnx(String key,Object value,long timeout){
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value,timeout, TimeUnit.SECONDS));
    }
    public Boolean exist(String key){
        Object value = get(key);
        return value != null;
    }
}
