package ru.practicum.stats.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.dto.NewHitDto;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsClientTest {

    @Mock
    private RestTemplate restTemplate;

    private StatsClient statsClient;

    @BeforeEach
    void setUp() {
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        String serverUrl = "http://localhost:9090";
        statsClient = new StatsClientImpl(serverUrl, builder);
    }

    @Test
    void saveHit_shouldPostWithCorrectData() {
        NewHitDto dto = NewHitDto.builder()
                .app("main-service")
                .uri("/events")
                .ip("192.168.1.1")
                .timestamp("2025-11-04 10:00:00")
                .build();

        when(restTemplate.exchange(
                eq("/hit"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok("Success"));

        ResponseEntity<Object> response = statsClient.saveHit(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Success");

        verify(restTemplate, times(1)).exchange(
                eq("/hit"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        );
    }

    @Test
    void getStats_withUris_shouldBuildCorrectUrl() {
        List<String> uris = List.of("/events/1", "/events/2");

        when(restTemplate.exchange(
                eq("/stats?start={start}&end={end}&uris={uris}&unique={unique}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                any(Map.class)
        )).thenReturn(ResponseEntity.ok("[{}, {}]"));

        ResponseEntity<Object> response = statsClient.getStats(
                "2025-11-01 00:00:00",
                "2025-11-05 00:00:00",
                uris,
                true
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[{}, {}]");

        verify(restTemplate, times(1)).exchange(
                eq("/stats?start={start}&end={end}&uris={uris}&unique={unique}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                argThat((Map<String, Object> params) ->
                        params.get("start").equals("2025-11-01 00:00:00") &&
                                params.get("end").equals("2025-11-05 00:00:00") &&
                                params.get("uris").equals("/events/1,/events/2") &&
                                params.get("unique").equals(true)
                )
        );
    }

    @Test
    void getStats_withoutUris_shouldBuildCorrectUrl() {
        when(restTemplate.exchange(
                eq("/stats?start={start}&end={end}&unique={unique}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                any(Map.class)
        )).thenReturn(ResponseEntity.ok("[]"));

        ResponseEntity<Object> response = statsClient.getStats(
                "2025-11-01 00:00:00",
                "2025-11-05 00:00:00",
                null,
                false
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[]");

        verify(restTemplate, times(1)).exchange(
                eq("/stats?start={start}&end={end}&unique={unique}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                argThat((Map<String, Object> params) ->
                        params.get("start").equals("2025-11-01 00:00:00") &&
                                params.get("end").equals("2025-11-05 00:00:00") &&
                                params.get("unique").equals(false) &&
                                !params.containsKey("uris")
                )
        );
    }

    @Test
    void getStats_withEmptyUris_shouldBuildUrlWithoutUris() {
        when(restTemplate.exchange(
                eq("/stats?start={start}&end={end}&unique={unique}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                any(Map.class)
        )).thenReturn(ResponseEntity.ok("[]"));

        ResponseEntity<Object> response = statsClient.getStats(
                "2025-11-01 00:00:00",
                "2025-11-05 00:00:00",
                List.of(),
                false
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[]");

        verify(restTemplate, times(1)).exchange(
                eq("/stats?start={start}&end={end}&unique={unique}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                any(Map.class)
        );
    }

    @Test
    void whenHttpError_shouldReturnErrorResponse() {
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn("Error message".getBytes(StandardCharsets.UTF_8));

        when(restTemplate.exchange(
                eq("/hit"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenThrow(exception);

        ResponseEntity<Object> response = statsClient.saveHit(NewHitDto.builder()
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp("2025-11-04 10:00:00")
                .build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void whenGenericException_shouldReturnInternalError() {
        when(restTemplate.exchange(
                eq("/hit"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenThrow(new RuntimeException("Connection failed"));

        ResponseEntity<Object> response = statsClient.saveHit(NewHitDto.builder()
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp("2025-11-04 10:00:00")
                .build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).asString().contains("Service unavailable");
    }
}