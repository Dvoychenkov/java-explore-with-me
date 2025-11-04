package ru.practicum.stats.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NewHitDtoTest extends BaseDtoTest {

    @Test
    void shouldSerializeToJson() throws Exception {
        NewHitDto dto = NewHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp("2025-11-04 10:00:00")
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertJsonContainsFields(json, "app", "uri", "ip", "timestamp");
        assertJsonDoesNotContainFields(json, "id"); // Не должно быть id
        assertThat(json).contains("\"app\":\"ewm-main-service\"");
        assertThat(json).contains("\"uri\":\"/events/1\"");
        assertThat(json).contains("\"ip\":\"192.168.1.1\"");
        assertThat(json).contains("\"timestamp\":\"2025-11-04 10:00:00\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        NewHitDto expected = NewHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp("2025-11-04 10:00:00")
                .build();

        String json = objectMapper.writeValueAsString(expected);

        NewHitDto actual = objectMapper.readValue(json, NewHitDto.class);

        assertThat(actual).isEqualTo(expected);
        assertThat(actual.getApp()).isEqualTo("ewm-main-service");
        assertThat(actual.getUri()).isEqualTo("/events/1");
        assertThat(actual.getIp()).isEqualTo("192.168.1.1");
        assertThat(actual.getTimestamp()).isEqualTo("2025-11-04 10:00:00");
    }

    @Test
    void shouldRoundTripSerialization() throws Exception {
        NewHitDto original = NewHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp("2025-11-04 10:00:00")
                .build();

        assertSerializationDeserialization(original, NewHitDto.class);
    }

    @Test
    void shouldHandleEmptyFields() throws Exception {
        NewHitDto dto = NewHitDto.builder()
                .app("")
                .uri("")
                .ip("")
                .timestamp("")
                .build();

        String json = objectMapper.writeValueAsString(dto);
        NewHitDto restored = objectMapper.readValue(json, NewHitDto.class);

        assertThat(restored.getApp()).isEmpty();
        assertThat(restored.getUri()).isEmpty();
        assertThat(restored.getIp()).isEmpty();
        assertThat(restored.getTimestamp()).isEmpty();
    }
}