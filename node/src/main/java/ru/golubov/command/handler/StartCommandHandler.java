package ru.golubov.command.handler;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.golubov.command.TelegramBotCommands;
import ru.golubov.command.TelegramBotCommandHandler;

@Component
public class StartCommandHandler implements TelegramBotCommandHandler {

    @Override
    public SendMessage processCommand(Update update) {
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("""
                      Привет, %s!
                      """.formatted(update.getMessage().getFrom().getFirstName()))
                .build();
    }

    @Override
    public TelegramBotCommands getSupportedCommand() {
        return TelegramBotCommands.START_COMMAND;
    }
}
