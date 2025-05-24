package ru.golubov.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RedisHistoryService {
    private static final String REQUEST_KEY_PREFIX = "user:context:";
    private static final int MAX_CONTEXT_ENTRIES = 3;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveUserContext(String userId, String request, String response) {
        try {
            String key = REQUEST_KEY_PREFIX + userId;
            // Формируем JSON для запроса
            Map<String, String> requestMap = Map.of("role", "user", "content", request);
            String requestJson = objectMapper.writeValueAsString(requestMap);
            // Формируем JSON для ответа
            Map<String, String> responseMap = Map.of("role", "assistant", "content", response);
            String responseJson = objectMapper.writeValueAsString(responseMap);

            // Добавляем запрос и ответ в начало списка
            redisTemplate.opsForList().leftPush(key, requestJson);
            redisTemplate.opsForList().leftPush(key, responseJson);
            // Ограничиваем список до 6 элементов (3 пары запрос-ответ)
            redisTemplate.opsForList().trim(key, 0, MAX_CONTEXT_ENTRIES * 2 - 1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user context to Redis", e);
        }
    }


    public List<Map<String, String>> getUserContext(String userId) {
        try {
            String key = REQUEST_KEY_PREFIX + userId;
            List<String> jsonList = redisTemplate.opsForList().range(key, 0, MAX_CONTEXT_ENTRIES * 2 - 1);
            List<Map<String, String>> context = new ArrayList<>();
            for (String json : jsonList) {
                context.add(objectMapper.readValue(json, Map.class));
            }
            return context.reversed();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve user context from Redis", e);
        }
    }

    public void clearUserContext(String userId) {
        try {
            String key = REQUEST_KEY_PREFIX + userId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear user context from Redis", e);
        }
    }
}
