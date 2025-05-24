package ru.golubov.ionet.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import ru.golubov.ionet.ChatCompletionResponse;
import ru.golubov.ionet.IoNetClient;
import ru.golubov.ionet.IoNetService;
import ru.golubov.service.RedisHistoryService;

@Service
@AllArgsConstructor
public class IoNetServiceImpl implements IoNetService {
    private final IoNetClient ioNetClient;
    private final RedisHistoryService redisHistoryService;
    @Override
    @NonNull
    public String getResponseFromChatToTextMessage(Long userId, String textInput) {
        ChatCompletionResponse chatCompletionResponse = ioNetClient.createChatCompletion(userId, textInput);

        try {
            String responseContent = chatCompletionResponse.choices().getFirst().message().content();
            redisHistoryService.saveUserContext(userId.toString(), textInput, responseContent);
            return responseContent;
        } catch (NullPointerException ignored) {
            redisHistoryService.saveUserContext(userId.toString(), textInput, " ");
        }
        return "Возникла ошибка";
    }
}
