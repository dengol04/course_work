package ru.golubov.ionet;

import java.util.List;

public record ChatCompletionRequest(
        String model,
        List<Message> messages
) {
}
