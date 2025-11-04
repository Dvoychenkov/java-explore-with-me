package ru.practicum.stats.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseDtoTest {
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    protected void assertSerializationDeserialization(Object original, Class<?> clazz) throws Exception {
        String json = objectMapper.writeValueAsString(original);
        System.out.println("Serialized JSON: " + json);

        Object restored = objectMapper.readValue(json, clazz);
        assertThat(restored).isEqualTo(original);
    }

    protected void assertJsonContainsFields(String json, String... expectedFields) {
        for (String field : expectedFields) {
            assertThat(json).contains("\"" + field + "\"");
        }
    }

    protected void assertJsonDoesNotContainFields(String json, String... unexpectedFields) {
        for (String field : unexpectedFields) {
            assertThat(json).doesNotContain("\"" + field + "\"");
        }
    }
}