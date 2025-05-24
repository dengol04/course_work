package ru.golubov.ionet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.golubov.service.RedisHistoryService;

@Configuration
public class IoNetConfiguration {
    @Bean
    public IoNetClient deepSeekClient(
            @Value("${ionet.token}") String deepSeekToken,
            @Value("${ionet.model}") String model,
            RestTemplateBuilder restTemplateBuilder,
            RedisHistoryService redisHistoryService
    ) {
        return new IoNetClient(deepSeekToken, model, restTemplateBuilder.build(), redisHistoryService);
    }
}
