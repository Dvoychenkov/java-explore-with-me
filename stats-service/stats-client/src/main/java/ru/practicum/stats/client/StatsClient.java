package ru.practicum.stats.client;

import jakarta.annotation.Nullable;
import org.springframework.http.ResponseEntity;
import ru.practicum.stats.dto.NewHitDto;

import java.util.List;

public interface StatsClient {

    ResponseEntity<Object> saveHit(NewHitDto newHitDto);

    ResponseEntity<Object> getStats(String start, String end, @Nullable List<String> uris, boolean unique);
}
