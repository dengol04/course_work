package ru.golubov.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.golubov.command.TelegramBotCommandsDispatcher;
import ru.golubov.dao.AppUserDAO;
import ru.golubov.dao.RawDataDAO;
import ru.golubov.ionet.IoNetClient;
import ru.golubov.entity.AppUser;
import ru.golubov.entity.RawData;
import ru.golubov.ionet.IoNetService;
import ru.golubov.service.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final IoNetService ioNetService;
    private final TelegramBotCommandsDispatcher telegramBotCommandsDispatcher;
    private final DocumentsExtractService documentsExtractService;
    @Value("${bot.token}")
    private String botToken;

    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, AppUserDAO appUserDAO, IoNetService ioNetService, TelegramBotCommandsDispatcher telegramBotCommandsDispatcher, DocumentsExtractService documentsExtractService) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.ioNetService = ioNetService;
        this.telegramBotCommandsDispatcher = telegramBotCommandsDispatcher;
        this.documentsExtractService = documentsExtractService;
    }

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

        var message = update.getMessage();
        var sendMessage = SendMessage.builder()
                .chatId(message.getChatId())
                .text(textResponse)
                .build();
        if (textResponse.length() <= 4096) {
            producerService.producerAnswer(sendMessage);
        } else {
            sendLargeMessage(sendMessage);
        }
    }

    private void sendLargeMessage(SendMessage sendMessage) {
        String textResponse = sendMessage.getText();
        for (int i = 0; i < textResponse.length(); i += 4000) {
            String part = textResponse.substring(i, Math.min(textResponse.length(), i + 4000));
            SendMessage partMessage = new SendMessage();
            partMessage.setChatId(sendMessage.getChatId());
            partMessage.setText(part);
            producerService.producerAnswer(partMessage);
        }
    }

    @Override
    public void processDocMessage(Update update) {
        if (!update.getMessage().hasDocument()) {
            throw new IllegalArgumentException("Message has no document");
        }
        var message = update.getMessage();
        var document = message.getDocument();
        var chatId = message.getChatId().toString();
        var fileName = document.getFileName().toLowerCase();

        String fileId = document.getFileId();
        String fileUrl;
        try {
            fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + documentsExtractService.getFilePath(fileId);
        } catch (TelegramApiException ignored) {
            fileUrl = "";
        }

        String text;
        try {
            if (fileName.endsWith(".pdf")) {
                text = documentsExtractService.extractTextFromPdf(fileUrl);
            } else if (fileName.endsWith(".docx")) {
                text = documentsExtractService.extractTextFromDocx(fileUrl);
            } else {
                text = documentsExtractService.extractTextFromTxt(fileUrl);
            }
        } catch (IOException ignored) {
            text = "";
        }
        if (text.isEmpty()) {
            sendErrorMessage(update, "Не удалось извлечь текст из документа.");
            return;
        }

        text += " " + update.getMessage().getText();
        update.getMessage().setText(text);
        processTextMessage(update);
    }

    private void sendErrorMessage(Update update, String text) {
        var sendMessage = SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(text)
                .build();

        producerService.producerAnswer(sendMessage);
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
