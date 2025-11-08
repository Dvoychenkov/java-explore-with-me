package ru.practicum.explorewithme.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.client.StatsClientImpl;

@Configuration
public class StatsClientConfig {

    @Bean
    public StatsClient statsClient(
            @Value("${stats-service.url}") String serverUrl,
            RestTemplateBuilder builder
    ) {
        return new StatsClientImpl(serverUrl, builder);
    }
}