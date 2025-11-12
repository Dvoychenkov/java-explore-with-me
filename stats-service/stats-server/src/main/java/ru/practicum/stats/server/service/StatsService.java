package ru.practicum.stats.server.service;

import jakarta.annotation.Nullable;
import ru.practicum.stats.dto.NewHitDto;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.util.List;

public interface StatsService {

    HitDto saveHit(NewHitDto hit);

    List<ViewStatsDto> getStats(String start, String end, @Nullable List<String> uris, boolean unique);
}
