package ru.golubov.service;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public interface DocumentsExtractService {
    String getFilePath(String fileId) throws TelegramApiException;

    String extractTextFromPdf(String fileUrl);

    String extractTextFromTxt(String fileUrl);

    String extractTextFromDocx(String fileUrl) throws IOException;
}