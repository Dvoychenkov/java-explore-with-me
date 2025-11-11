package ru.practicum.stats.server.repository;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.model.Hit;
import ru.practicum.stats.server.util.DateTimeUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("test")
class HitRepositoryTest {

    private final HitRepository hitRepository;

    @Test
    void aggregateQueries_shouldWork() {
        Hit h1 = new Hit();
        h1.setApp("ewm-main-service");
        h1.setUri("/events");
        h1.setIp("10.0.0.1");
        h1.setTimestamp(DateTimeUtils.fromString("2025-11-04 12:00:00"));
        hitRepository.save(h1);

        Hit h2 = new Hit();
        h2.setApp("ewm-main-service");
        h2.setUri("/events/1");
        h2.setIp("10.0.0.2");
        h2.setTimestamp(DateTimeUtils.fromString("2025-11-04 12:05:00"));
        hitRepository.save(h2);

        Hit h3 = new Hit();
        h3.setApp("ewm-main-service");
        h3.setUri("/events/1");
        h3.setIp("10.0.0.2");
        h3.setTimestamp(DateTimeUtils.fromString("2025-11-04 12:06:00"));
        hitRepository.save(h3);

        LocalDateTime start = DateTimeUtils.fromString("2025-11-04 00:00:00");
        LocalDateTime end = DateTimeUtils.fromString("2025-11-05 00:00:00");

        List<ViewStatsDto> nonUniqueAll = hitRepository.aggregateAll(start, end);

        List<ViewStatsDto> uniqueAll = hitRepository.aggregateUniqueAll(start, end);

        List<String> uris = Arrays.asList("/events", "/events/1");
        List<ViewStatsDto> nonUniqueFiltered = hitRepository.aggregate(start, end, uris);
        List<ViewStatsDto> uniqueFiltered = hitRepository.aggregateUnique(start, end, uris);

        Assertions.assertThat(nonUniqueAll)
                .anySatisfy(v -> {
                    if ("/events".equals(v.getUri())) {
                        Assertions.assertThat(v.getHits()).isEqualTo(1L);
                    }
                })
                .anySatisfy(v -> {
                    if ("/events/1".equals(v.getUri())) {
                        Assertions.assertThat(v.getHits()).isEqualTo(2L);
                    }
                });

        Assertions.assertThat(uniqueAll)
                .anySatisfy(v -> {
                    if ("/events".equals(v.getUri())) {
                        Assertions.assertThat(v.getHits()).isEqualTo(1L);
                    }
                })
                .anySatisfy(v -> {
                    if ("/events/1".equals(v.getUri())) {
                        Assertions.assertThat(v.getHits()).isEqualTo(1L);
                    }
                });

        Assertions.assertThat(nonUniqueFiltered)
                .anySatisfy(v -> {
                    if ("/events".equals(v.getUri())) {
                        Assertions.assertThat(v.getHits()).isEqualTo(1L);
                    }
                })
                .anySatisfy(v -> {
                    if ("/events/1".equals(v.getUri())) {
                        Assertions.assertThat(v.getHits()).isEqualTo(2L);
                    }
                });

        Assertions.assertThat(uniqueFiltered)
                .anySatisfy(v -> {
                    if ("/events".equals(v.getUri())) {
                        Assertions.assertThat(v.getHits()).isEqualTo(1L);
                    }
                })
                .anySatisfy(v -> {
                    if ("/events/1".equals(v.getUri())) {
                        Assertions.assertThat(v.getHits()).isEqualTo(1L);
                    }
                });
    }
}
