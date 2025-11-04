package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.stats.dto.NewHitDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StatsClientImpl extends BaseClient implements StatsClient {

    public StatsClientImpl(@Value("${stats-service.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build()
        );
    }

    @Override
    public ResponseEntity<Object> saveHit(NewHitDto newHitDto) {
        log.info("Send saveHit request, data: {}", newHitDto);
        return post("/hit", newHitDto);
    }

    @Override
    public ResponseEntity<Object> getStats(String start, String end, List<String> uris, boolean unique) {
        // Создаем базовые параметры
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", start);
        parameters.put("end", end);
        parameters.put("unique", unique);

        // Обрабатываем uris в зависимости от их наличия
        String queryPlaceholders;
        if (uris != null && !uris.isEmpty()) {
            String urisString = String.join(",", uris);
            parameters.put("uris", urisString);

            queryPlaceholders = "start={start}&end={end}&uris={uris}&unique={unique}";
        } else {
            queryPlaceholders = "start={start}&end={end}&unique={unique}";
        }

        log.info("Send getStats request, data: {}", parameters);
        return get("/stats?" + queryPlaceholders, parameters);
    }
}
