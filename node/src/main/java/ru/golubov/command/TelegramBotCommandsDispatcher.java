package ru.golubov.command;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class TelegramBotCommandsDispatcher {
    @Autowired
    private final List<TelegramBotCommandHandler> telegramBotCommandHandlerList;

    public SendMessage processCommand(Update update) {
        if (!isCommand(update)) {
            throw new IllegalArgumentException("Not a command");
        }
        var text = update.getMessage().getText();
        var correctHandler = telegramBotCommandHandlerList
                .stream()
                .filter(x -> Objects.equals(x.getSupportedCommand().getCommand(), text))
                .findAny();
        if (correctHandler.isEmpty()) {
            var sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            sendMessage.setText("Такая команда не поддерживается: %s".formatted(text));

            return sendMessage;
        }

        return correctHandler.orElseThrow().processCommand(update);
    }

    public boolean isCommand(Update update) {
        return update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().startsWith("/");
    }
}
