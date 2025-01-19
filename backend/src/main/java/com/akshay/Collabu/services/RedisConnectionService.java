package com.akshay.Collabu.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisConnectionService {

    public final RedisTemplate<String, String> redisTemplate;

    public RedisConnectionService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean checkConnection() {
        try {
            // Ping the Redis server
            String response = redisTemplate.getConnectionFactory().getConnection().ping();
            return "PONG".equals(response);
        } catch (Exception e) {
            // Handle exceptions (e.g., Redis is down, network issue)
        	e.printStackTrace();
            return false;
        }
    }
}

