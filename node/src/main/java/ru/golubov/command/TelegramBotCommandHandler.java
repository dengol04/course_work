package ru.golubov.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramBotCommandHandler {
    SendMessage processCommand(Update update);
    TelegramBotCommands getSupportedCommand();
}
