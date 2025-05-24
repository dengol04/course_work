package ru.golubov.command.handler;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.golubov.command.TelegramBotCommands;
import ru.golubov.command.TelegramBotCommandHandler;
import ru.golubov.dao.AppUserDAO;
import ru.golubov.service.RedisHistoryService;

@Component
@AllArgsConstructor
public class ClearCommandHandler implements TelegramBotCommandHandler {
    private final RedisHistoryService redisHistoryService;
    private final AppUserDAO appUserDAO;

    @Override
    public SendMessage processCommand(Update update) {
        Long telegramUserId = update.getMessage().getFrom().getId();
        Long userId = appUserDAO.findAppUserByTelegramUserId(telegramUserId).getId();
        redisHistoryService.clearUserContext(userId.toString());
        return SendMessage.builder().chatId(update.getMessage().getChatId()).text("История котекста сброшена.").build();
    }

    @Override
    public TelegramBotCommands getSupportedCommand() {
        return TelegramBotCommands.CLEAR_COMMAND;
    }
}
