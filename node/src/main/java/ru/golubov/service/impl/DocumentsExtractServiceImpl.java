package ru.golubov.service.impl;

import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.golubov.service.DocumentsExtractService;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class DocumentsExtractServiceImpl implements DocumentsExtractService {
    @Value("${bot.token}")
    private String botToken;
    private final RestTemplate restTemplate;

    public DocumentsExtractServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getFilePath(String fileId) throws TelegramApiException {
        String url = "https://api.telegram.org/bot" + botToken + "/getFile?file_id=" + fileId;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response != null && (boolean) response.get("ok")) {
            return (String) ((Map<?, ?>) response.get("result")).get("file_path");
        }
        throw new TelegramApiException("Не удалось получить путь к файлу.");
    }

    @Override
    public String extractTextFromPdf(String fileUrl) {
        try (InputStream inputStream = new URL(fileUrl).openStream();
             PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String extractTextFromTxt(String fileUrl) {
        try (InputStream inputStream = new URL(fileUrl).openStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String extractTextFromDocx(String fileUrl) throws IOException {
        try (InputStream inputStream = new URL(fileUrl).openStream();
             XWPFDocument document = new XWPFDocument(inputStream)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }
}