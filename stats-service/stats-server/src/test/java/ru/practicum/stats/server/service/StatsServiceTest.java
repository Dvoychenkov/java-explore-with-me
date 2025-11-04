package ru.practicum.stats.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.stats.dto.NewHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.model.Hit;
import ru.practicum.stats.server.mapper.HitMapper;
import ru.practicum.stats.server.repository.HitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private HitRepository hitRepository;

    private final HitMapper hitMapper = Mappers.getMapper(HitMapper.class);

    private StatsServiceImpl statsService;

    private static final DateTimeFormatter ISO_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        statsService = new StatsServiceImpl(hitRepository, hitMapper);
    }

    @Test
    void save_Hit_shouldPersistEntity() {
        NewHitDto dto = NewHitDto.builder()
                .app("ewm-main-service")
                .uri("/events")
                .ip("127.0.0.1")
                .timestamp("2025-11-04 10:00:00")
                .build();

        ArgumentCaptor<Hit> captor = ArgumentCaptor.forClass(Hit.class);

        statsService.saveHit(dto);

        verify(hitRepository, times(1)).save(captor.capture());
        Hit saved = captor.getValue();
        assertThat(saved.getApp()).isEqualTo("ewm-main-service");
        assertThat(saved.getUri()).isEqualTo("/events");
        assertThat(saved.getIp()).isEqualTo("127.0.0.1");
        assertThat(saved.getTimestamp()).isEqualTo(LocalDateTime.parse("2025-11-04 10:00:00", ISO_DATE_TIME_FORMAT));
    }

    @Test
    void getStats_shouldAggregateWithUris() {
        LocalDateTime start = LocalDateTime.parse("2025-11-01 00:00:00", ISO_DATE_TIME_FORMAT);
        LocalDateTime end = LocalDateTime.parse("2025-11-05 00:00:00", ISO_DATE_TIME_FORMAT);

        List<ViewStatsDto> repoResult = Arrays.asList(
                ViewStatsDto.builder().app("app").uri("/events").hits(5L).build(),
                ViewStatsDto.builder().app("app").uri("/events/1").hits(2L).build()
        );

        when(hitRepository.aggregate(start, end, Arrays.asList("/events", "/events/1")))
                .thenReturn(repoResult);

        List<ViewStatsDto> result = statsService.getStats(
                "2025-11-01 00:00:00",
                "2025-11-05 00:00:00",
                Arrays.asList("/events", "/events/1"),
                false
        );

        verify(hitRepository, times(1)).aggregate(start, end, Arrays.asList("/events", "/events/1"));
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getHits()).isEqualTo(5L);
    }

    @Test
    void getStats_shouldAggregateUniqueAll() {
        LocalDateTime start = LocalDateTime.parse("2025-11-01 00:00:00", ISO_DATE_TIME_FORMAT);
        LocalDateTime end = LocalDateTime.parse("2025-11-05 00:00:00", ISO_DATE_TIME_FORMAT);

        List<ViewStatsDto> repoResult = Collections.singletonList(
                ViewStatsDto.builder().app("app").uri("/events").hits(3L).build()
        );

        when(hitRepository.aggregateUniqueAll(start, end)).thenReturn(repoResult);

        List<ViewStatsDto> result = statsService.getStats(
                "2025-11-01 00:00:00",
                "2025-11-05 00:00:00",
                null,
                true
        );

        verify(hitRepository, times(1)).aggregateUniqueAll(start, end);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHits()).isEqualTo(3L);
    }
}
