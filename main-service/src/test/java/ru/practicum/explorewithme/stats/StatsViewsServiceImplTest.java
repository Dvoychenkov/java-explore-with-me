package ru.practicum.explorewithme.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.stats.client.StatsClient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsViewsServiceImplTest {

    @Mock
    private StatsClient statsClient;

    private StatsViewsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StatsViewsServiceImpl(statsClient, new ObjectMapper());
    }

    @Test
    void fetchViews_mapsResponseToUriHits() {
        Map<String, Object> v1 = new HashMap<>();
        v1.put("app", "ewm");
        v1.put("uri", "/events/1");
        v1.put("hits", 42);

        Map<String, Object> v2 = new HashMap<>();
        v2.put("app", "ewm");
        v2.put("uri", "/events/2");
        v2.put("hits", 7);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> body = Arrays.asList(v1, v2);

        when(statsClient.getStats(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyList(), ArgumentMatchers.eq(Boolean.TRUE)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        Map<String, Long> out = service.fetchViews(LocalDateTime.now().minusDays(1),
                LocalDateTime.now(), List.of("/events/1", "/events/2"), true);

        assertThat(out.get("/events/1")).isEqualTo(42L);
        assertThat(out.get("/events/2")).isEqualTo(7L);
    }

    @Test
    void fetchViews_non2xx_returnsEmpty() {
        when(statsClient.getStats(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyList(), ArgumentMatchers.eq(Boolean.TRUE)))
                .thenReturn(new ResponseEntity<>("err", HttpStatus.BAD_GATEWAY));

        Map<String, Long> out = service.fetchViews(LocalDateTime.now().minusDays(1),
                LocalDateTime.now(), List.of("/events/1"), true);

        assertThat(out).isEmpty();
    }
}
