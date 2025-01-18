package com.akshay.Collabu.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.akshay.Collabu.services.RedisConnectionService;

@RestController
public class RedisController {
    @Autowired
    private RedisConnectionService redisConnectionService;

    @GetMapping("/test-redis")
    public String testRedis() {
    	boolean result = redisConnectionService.checkConnection();
    	System.out.println(result);
        redisConnectionService.redisTemplate.opsForValue().set("testKey", "Hello, Redis!");
        return "Redis says: " + redisConnectionService.redisTemplate.opsForValue().get("testKey");
    }
}
