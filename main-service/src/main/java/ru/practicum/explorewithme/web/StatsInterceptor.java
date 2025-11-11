package ru.practicum.explorewithme.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.practicum.explorewithme.util.DateTimeUtils;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.NewHitDto;

import java.time.LocalDateTime;

import static ru.practicum.explorewithme.util.AppConstants.EVENTS_URL_PREFIX;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsInterceptor implements HandlerInterceptor {

    private final StatsClient statsClient;

    @Value("${stats-service.app-name}")
    private String appName;

    // Отправляем статистику после завершения запроса
    @Override
    public void afterCompletion(
            HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Фильтруем только нужные эндпоинты
        if (!"GET".equalsIgnoreCase(method) && uri.matches(EVENTS_URL_PREFIX + "\\d+")) {
            return;
        }

        NewHitDto hit = NewHitDto.builder()
                .app(appName)
                .uri(uri)
                .ip(request.getRemoteAddr())
                .timestamp(DateTimeUtils.toString(LocalDateTime.now()))
                .build();

        sendStats(hit);
    }

    private void sendStats(NewHitDto hit) {
        try {
            ResponseEntity<Object> response = statsClient.saveHit(hit);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to save stats for hit: {}, status: {}", hit, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error saving stats for hit: {}", hit, e);
        }
    }
}