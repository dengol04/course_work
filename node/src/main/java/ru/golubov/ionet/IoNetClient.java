package ru.golubov.ionet;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.golubov.service.RedisHistoryService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Log4j
public class IoNetClient {
    @Value("${ionet.token}")
    private final String token;
    @Value("${ionet.model}")
    private final String model;
    private final RestTemplate restTemplate;
    private final RedisHistoryService redisHistoryService;

    @SneakyThrows
    public ChatCompletionResponse createChatCompletion(Long userId, String message) {
        String url = "https://api.intelligence.io.solutions/api/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = createRequestBody(userId.toString(), message);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);


        return restTemplate.exchange(url, HttpMethod.POST, entity, ChatCompletionResponse.class).getBody();
    }

    private String createRequestBody(String userId, String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, String>> messages = new ArrayList<>();

            messages.add(Map.of("role", "system", "content", "You are a helpful assistant."));

            List<Map<String, String>> context = redisHistoryService.getUserContext(userId);

            List<Map<String, String>> filteredContext = new ArrayList<>();
            String lastRole = "system";
            for (Map<String, String> msg : context) {
                String currentRole = msg.get("role");
                if (lastRole.equals("system") && currentRole.equals("user") ||
                        lastRole.equals("user") && currentRole.equals("assistant") ||
                        lastRole.equals("assistant") && currentRole.equals("user")) {
                    filteredContext.add(msg);
                    lastRole = currentRole;
                }
            }

            messages.addAll(filteredContext);

            if (!lastRole.equals("user")) {
                messages.add(Map.of("role", "user", "content", message));
            } else {
                throw new IllegalStateException("Cannot add user message after another user message");
            }

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", messages
            );

            String jsonBody = mapper.writeValueAsString(body);
            log.info("Формируемый JSON: %s".formatted(jsonBody));
            return jsonBody;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JSON request body", e);
        }
    }
}
