package ru.practicum.stats.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HitDtoTest extends BaseDtoTest {

    @Test
    void shouldSerializeToJson() throws Exception {
        HitDto hit = new HitDto(1L, "ewm-main-service", "/events/1", "192.168.1.1", "2025-11-04 10:00:00");

        String json = objectMapper.writeValueAsString(hit);

        assertJsonContainsFields(json, "id", "app", "uri", "ip", "timestamp");
        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"app\":\"ewm-main-service\"");
        assertThat(json).contains("\"uri\":\"/events/1\"");
        assertThat(json).contains("\"ip\":\"192.168.1.1\"");
        assertThat(json).contains("\"timestamp\":\"2025-11-04 10:00:00\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        HitDto expected = new HitDto(1L, "ewm-main-service", "/events/1", "192.168.1.1", "2025-11-04 10:00:00");

        String json = objectMapper.writeValueAsString(expected);
        HitDto actual = objectMapper.readValue(json, HitDto.class);

        assertThat(actual).isEqualTo(expected);
        assertThat(actual.getId()).isEqualTo(1L);
        assertThat(actual.getApp()).isEqualTo("ewm-main-service");
        assertThat(actual.getUri()).isEqualTo("/events/1");
        assertThat(actual.getIp()).isEqualTo("192.168.1.1");
        assertThat(actual.getTimestamp()).isEqualTo("2025-11-04 10:00:00");
    }

    @Test
    void shouldRoundTripSerialization() throws Exception {
        HitDto original = new HitDto(1L, "ewm-main-service", "/events/1", "192.168.1.1", "2025-11-04 10:00:00");
        assertSerializationDeserialization(original, HitDto.class);
    }

    @Test
    void shouldHandleNullId() throws Exception {
        HitDto hit = new HitDto(null, "app", "/uri", "ip", "timestamp");

        String json = objectMapper.writeValueAsString(hit);
        HitDto restored = objectMapper.readValue(json, HitDto.class);

        assertThat(restored.getId()).isNull();
        assertThat(restored.getApp()).isEqualTo("app");
    }
}