package ru.golubov.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.golubov.command.TelegramBotCommandsDispatcher;
import ru.golubov.dao.AppUserDAO;
import ru.golubov.dao.RawDataDAO;
import ru.golubov.ionet.IoNetClient;
import ru.golubov.entity.AppUser;
import ru.golubov.entity.RawData;
import ru.golubov.ionet.IoNetService;
import ru.golubov.service.MainService;
import ru.golubov.service.ProducerService;
import ru.golubov.service.RedisHistoryService;
import ru.golubov.service.UserRequestService;

@AllArgsConstructor
@Service
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final IoNetService ioNetService;
    private final TelegramBotCommandsDispatcher telegramBotCommandsDispatcher;

    @Override
    public void processTextMessage(Update update) {
        if (telegramBotCommandsDispatcher.isCommand(update)) {
            producerService.producerAnswer(telegramBotCommandsDispatcher.processCommand(update));
            return;
        }
        saveRawData(update);
        var textMessage = update.getMessage();
        var telegramUser = textMessage.getFrom();
        var appUser = findOrSaveAppUser(telegramUser);

        var textResponse = ioNetService.getResponseFromChatToTextMessage(appUser.getId(), textMessage.getText());

        String escapedText = escapeMarkdownV2(textResponse);

        var message = update.getMessage();
        var sendMessage = SendMessage.builder()
                .chatId(message.getChatId())
                .text(escapedText)
                .parseMode("Markdown")
                .build();
        if (escapedText.length() <= 4096) {
            producerService.producerAnswer(sendMessage);
        } else {
            for (int i = 0; i < escapedText.length(); i += 4000) {
                String part = escapedText.substring(i, Math.min(escapedText.length(), i + 4000));
                SendMessage partMessage = new SendMessage();
                partMessage.setChatId(sendMessage.getChatId());
                partMessage.setParseMode("Markdown");
                partMessage.setText(part);
                producerService.producerAnswer(partMessage);
            }
        }
    }

    private String escapeMarkdownV2(String text) {
        if (text == null) {
            return "";
        }
        // Экранируем все зарезервированные символы MarkdownV2
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

    private AppUser findOrSaveAppUser(User telegramUser) {
        AppUser persistentAppUser = appUserDAO.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                        .event(update)
                        .build();
        rawDataDAO.save(rawData);
    }
}
