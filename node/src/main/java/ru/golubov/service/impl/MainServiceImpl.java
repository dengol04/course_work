package ru.golubov.service.impl;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.golubov.dao.AppUserDAO;
import ru.golubov.dao.RawDataDAO;
import ru.golubov.ionet.IoNetClient;
import ru.golubov.entity.AppUser;
import ru.golubov.entity.RawData;
import ru.golubov.service.MainService;
import ru.golubov.service.ProducerService;
import ru.golubov.service.RedisHistoryService;
import ru.golubov.service.UserRequestService;

@Service
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final UserRequestService userRequestService;
    private final IoNetClient ioNetClient;
    private final RedisHistoryService redisHistoryService;

    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, AppUserDAO appUserDAO, UserRequestService userRequestService, IoNetClient ioNetClient, RedisHistoryService redisHistoryService) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.userRequestService = userRequestService;
        this.ioNetClient = ioNetClient;
        this.redisHistoryService = redisHistoryService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var textMessage = update.getMessage();
        var telegramUser = textMessage.getFrom();
        var appUser = findOrSaveAppUser(telegramUser);

        //userRequestService.saveUserRequest(appUser.getId(), textMessage.getText());
        redisHistoryService.saveUserRequest(appUser.getId().toString(), textMessage.getText());

        var chatCompletionResponse = ioNetClient.createChatCompletion(textMessage.getText());
        var textResponse = chatCompletionResponse
                .choices()
                .getFirst()
                .message()
                .content();

        redisHistoryService.saveUserResponse(appUser.getId().toString(), textResponse);

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(textResponse);
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
