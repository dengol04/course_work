package ru.golubov.ionet;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class IoNetClient {
    @Value("${ionet.token}")
    private final String token;
    @Value("${ionet.model}")
    private final String model;
    private final RestTemplate restTemplate;

    @SneakyThrows
    public ChatCompletionResponse createChatCompletion(String message) {
        String url = "https://api.intelligence.io.solutions/api/v1/chat/completions";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + token);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> httpEntity = new HttpEntity<>(getJsonForHttpEntity(message), httpHeaders);

        ResponseEntity<ChatCompletionResponse> responseEntity = restTemplate.exchange(
                url, HttpMethod.POST, httpEntity, ChatCompletionResponse.class
        );

        return responseEntity.getBody();
    }

    @SneakyThrows
    private String getJsonForHttpEntity(String message) {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "You are a helpful assistant."));
        messages.add(Map.of("role", "user", "content", message));
        requestBody.put("messages", messages);

        return mapper.writeValueAsString(requestBody);
    }
}
