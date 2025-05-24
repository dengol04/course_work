package ru.golubov.ionet.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import ru.golubov.ionet.IoNetClient;
import ru.golubov.ionet.IoNetService;

@Service
@AllArgsConstructor
public class IoNetServiceImpl implements IoNetService {
    private final IoNetClient ioNetClient;
    @Override
    @NonNull
    public String getResponseFromChatToTextMessage(Long userId, String textInput) {
        return null;
    }
}
