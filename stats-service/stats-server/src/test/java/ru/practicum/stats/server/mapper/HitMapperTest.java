package ru.practicum.stats.server.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.stats.dto.NewHitDto;
import ru.practicum.stats.server.model.Hit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class HitMapperTest {

    private final HitMapper hitMapper = Mappers.getMapper(HitMapper.class);
    private static final DateTimeFormatter ISO_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void toEntity_shouldMap() {
        NewHitDto dto = NewHitDto.builder()
                .app("ewm-main-service")
                .uri("/events")
                .ip("127.0.0.1")
                .timestamp("2025-11-04 10:00:00")
                .build();

        Hit entity = hitMapper.toHit(dto);

        Assertions.assertThat(entity.getApp()).isEqualTo("ewm-main-service");
        Assertions.assertThat(entity.getUri()).isEqualTo("/events");
        Assertions.assertThat(entity.getIp()).isEqualTo("127.0.0.1");
        Assertions.assertThat(entity.getTimestamp()).isEqualTo(LocalDateTime.parse("2025-11-04 10:00:00", ISO_DATE_TIME_FORMAT));
    }
}
