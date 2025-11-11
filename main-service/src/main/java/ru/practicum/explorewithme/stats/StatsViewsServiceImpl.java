package ru.practicum.explorewithme.stats;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.util.DateTimeUtils;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStatsDto;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsViewsServiceImpl implements StatsViewsService {

    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Long> fetchViews(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (uris == null || uris.isEmpty()) {
            return Collections.emptyMap();
        }

        DateTimeUtils.validateDateRange(start, end);
        String startStr = DateTimeUtils.toString(start);
        String endStr = DateTimeUtils.toString(end);

        String queryParams = String.format("start: %s, end: %s, uris: %s, unique: %b", startStr, endStr, uris, unique);
        List<ViewStatsDto> viewStatsDtos;
        try {
            log.debug("Fetching views statistics for: {}", queryParams);
            ResponseEntity<Object> getStatsResponse = statsClient.getStats(startStr, endStr, uris, unique);

            viewStatsDtos = processStatsResponse(getStatsResponse, queryParams);
        } catch (Exception e) {
            log.error("Failed to fetch views statistics for {}: {}", queryParams, e.getMessage(), e);
            return Collections.emptyMap();
        }

        return convertToUriHitsMap(viewStatsDtos, queryParams);
    }

    private List<ViewStatsDto> processStatsResponse(ResponseEntity<Object> response, String queryParams) {
        // Проверяем статус
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("Stats service returned non-2xx status for {}: {}",
                    queryParams, response.getStatusCode());
            return Collections.emptyList();
        }

        // Проверяем наличие тела ответа
        if (response.getBody() == null) {
            log.warn("Stats service returned empty body for: {}", queryParams);
            return Collections.emptyList();
        }

        try {
            return objectMapper.convertValue(
                    response.getBody(), new TypeReference<>() {
                    }
            );

        } catch (IllegalArgumentException e) {
            log.error("Failed to parse stats response body for {}: {}", queryParams, e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error while processing stats response for {}: {}", queryParams, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Map<String, Long> convertToUriHitsMap(List<ViewStatsDto> viewStatsDtos, String queryParams) {
        if (viewStatsDtos == null || viewStatsDtos.isEmpty()) {
            log.debug("No view stats data found for: {}", queryParams);
            return Collections.emptyMap();
        }

        Map<String, Long> uriToHits = new HashMap<>();
        int validEntries = 0;

        for (ViewStatsDto viewStatsDto : viewStatsDtos) {
            if (isValidViewStatsDto(viewStatsDto)) {
                uriToHits.put(viewStatsDto.getUri(), viewStatsDto.getHits());
                validEntries++;
            }
        }

        log.debug("Successfully processed {} view stats entries out of {} for: {}",
                validEntries, viewStatsDtos.size(), queryParams);

        return uriToHits;
    }

    private boolean isValidViewStatsDto(ViewStatsDto viewStatsDto) {
        if (viewStatsDto == null) {
            return false;
        }
        if (viewStatsDto.getUri() == null || viewStatsDto.getUri().trim().isEmpty()) {
            log.warn("Found ViewStatsDto with null or empty URI: {}", viewStatsDto);
            return false;
        }
        if (viewStatsDto.getHits() == null || viewStatsDto.getHits() < 0) {
            log.warn("Found ViewStatsDto with invalid hits count: {}", viewStatsDto);
            return false;
        }
        return true;
    }
}
