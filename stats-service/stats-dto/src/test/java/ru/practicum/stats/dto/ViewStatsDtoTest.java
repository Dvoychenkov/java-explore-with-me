package ru.practicum.stats.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViewStatsDtoTest extends BaseDtoTest {

    @Test
    void shouldSerializeToJson() throws Exception {
        ViewStatsDto dto = ViewStatsDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .hits(42L)
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertJsonContainsFields(json, "app", "uri", "hits");
        assertThat(json).contains("\"app\":\"ewm-main-service\"");
        assertThat(json).contains("\"uri\":\"/events/1\"");
        assertThat(json).contains("\"hits\":42");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        ViewStatsDto expected = ViewStatsDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .hits(42L)
                .build();

        String json = objectMapper.writeValueAsString(expected);

        ViewStatsDto actual = objectMapper.readValue(json, ViewStatsDto.class);

        assertThat(actual).isEqualTo(expected);
        assertThat(actual.getApp()).isEqualTo("ewm-main-service");
        assertThat(actual.getUri()).isEqualTo("/events/1");
        assertThat(actual.getHits()).isEqualTo(42L);
    }

    @Test
    void shouldRoundTripSerialization() throws Exception {
        ViewStatsDto original = ViewStatsDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .hits(42L)
                .build();

        assertSerializationDeserialization(original, ViewStatsDto.class);
    }

    @Test
    void shouldHandleZeroHits() throws Exception {
        ViewStatsDto dto = ViewStatsDto.builder()
                .app("new-app")
                .uri("/new-uri")
                .hits(0L)
                .build();

        String json = objectMapper.writeValueAsString(dto);
        ViewStatsDto restored = objectMapper.readValue(json, ViewStatsDto.class);

        assertThat(restored.getHits()).isZero();
    }

    @Test
    void shouldHandleNullFields() throws Exception {
        ViewStatsDto dto = ViewStatsDto.builder()
                .app(null)
                .uri(null)
                .hits(null)
                .build();

        String json = objectMapper.writeValueAsString(dto);
        ViewStatsDto restored = objectMapper.readValue(json, ViewStatsDto.class);

        assertThat(restored.getApp()).isNull();
        assertThat(restored.getUri()).isNull();
        assertThat(restored.getHits()).isNull();
    }
}