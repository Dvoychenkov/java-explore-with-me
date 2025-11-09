package ru.practicum.explorewithme.stats;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.explorewithme.util.DateTimeUtils.ISO_DATE_TIME_FORMATTER;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsViewsServiceImpl implements StatsViewsService {

    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    // TODO return ViewStatsDto?
    @Override
    public Map<String, Long> fetchViews(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (uris == null || uris.isEmpty()) {
            return Collections.emptyMap();
        }

        // TODO url encode?
        String startStr = start.format(ISO_DATE_TIME_FORMATTER);
        String endStr = end.format(ISO_DATE_TIME_FORMATTER);

        // TODO add try catch?
        ResponseEntity<Object> getStatsResponse = statsClient.getStats(startStr, endStr, uris, unique);

        if (!getStatsResponse.getStatusCode().is2xxSuccessful()) {
            String queryParams = String.format("[start: %s, ens: %s, uris: %s, unique: %b]",
                    startStr, endStr, uris, unique);
            log.error("getStats returns incorrect response, queryParams: {}, status: {}, resp body: {}, resp headers: {}",
                    queryParams, getStatsResponse.getStatusCode(), getStatsResponse.getBody(), getStatsResponse.getHeaders());
            return Collections.emptyMap();
        }
        if (getStatsResponse.getBody() == null) {
            log.warn("Stats service response has empty body");
            return Collections.emptyMap();
        }

        try {
            List<ViewStatsDto> viewStatsDtos = objectMapper.convertValue(getStatsResponse.getBody(), new TypeReference<>() {
            });

            Map<String, Long> uriToHits = new HashMap<>();
            for (ViewStatsDto viewStatsDto : viewStatsDtos) {
                uriToHits.put(viewStatsDto.getUri(), viewStatsDto.getHits());
            }

            return uriToHits;
        } catch (IllegalArgumentException ex) {
            log.error("Failed to parse stats response body: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }
}
