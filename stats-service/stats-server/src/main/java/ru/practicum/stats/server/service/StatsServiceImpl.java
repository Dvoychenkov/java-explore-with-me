package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.NewHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.mapper.HitMapper;
import ru.practicum.stats.server.model.Hit;
import ru.practicum.stats.server.repository.HitRepository;
import ru.practicum.stats.server.util.DateTimeUtils;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.stats.server.util.DateTimeUtils.ISO_DATE_TIME_FORMATTER;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final HitRepository repo;
    private final HitMapper hitMapper;

    @Override
    public HitDto saveHit(NewHitDto newHitDto) {
        Hit savedHit = repo.save(hitMapper.toHit(newHitDto));
        log.info("Hit saved success: {}", savedHit);
        return hitMapper.toHitDto(savedHit);
    }

    @Override
    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {

        LocalDateTime startDt = DateTimeUtils.fromString(start);
        LocalDateTime endDt = DateTimeUtils.fromString(end);
        DateTimeUtils.validateDateRange(startDt, endDt);

        List<ViewStatsDto> statsRows;

        if (uris != null && !uris.isEmpty()) {
            statsRows = unique ?
                    repo.aggregateUnique(startDt, endDt, uris) :
                    repo.aggregate(startDt, endDt, uris);
        } else {
            statsRows = unique ?
                    repo.aggregateUniqueAll(startDt, endDt) :
                    repo.aggregateAll(startDt, endDt);
        }

        log.info("Get stats success: start: {}; end: {}; uris: {}; unique: {}; result: {}",
                start, end, uris, unique, statsRows);

        return statsRows;
    }
}
