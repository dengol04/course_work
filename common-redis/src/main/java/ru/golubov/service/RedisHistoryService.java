package ru.golubov.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RedisHistoryService {
    private static final String REQUEST_KEY_PREFIX = "user:requests:";
    private static final String RESPONSE_KEY_PREFIX = "user:responses:";
    private static final int MAX_REQUESTS = 3;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void saveUserRequest(String userId, String request) {
        String key = REQUEST_KEY_PREFIX + userId;
        redisTemplate.opsForList().leftPush(key, request);
        redisTemplate.opsForList().trim(key, 0, MAX_REQUESTS - 1);
    }

    public void saveUserResponse(String userId, String response) {
        String key = RESPONSE_KEY_PREFIX + userId;
        redisTemplate.opsForList().leftPush(key, response);
        redisTemplate.opsForList().trim(key, 0, MAX_REQUESTS - 1);
    }

    public List<String> getUserRequests(String userId) {
        String key = REQUEST_KEY_PREFIX + userId;
        return redisTemplate.opsForList().range(key, 0, MAX_REQUESTS - 1);
    }

    public List<String> getUserResponses(String userId) {
        String key = RESPONSE_KEY_PREFIX + userId;
        return redisTemplate.opsForList().range(key, 0, MAX_REQUESTS - 1);
    }
}
