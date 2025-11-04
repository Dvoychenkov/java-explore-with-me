package ru.practicum.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HitRepository extends JpaRepository<Hit, Long> {

    // Все запросы статистики по указанным эндпоинтам за временной интервал
    @Query("""
                select new ru.practicum.stats.dto.ViewStatsDto(h.app, h.uri, count(h))
                from Hit h
                where h.timestamp between :start and :end
                    and h.uri in :uris
                group by h.app, h.uri
                order by count(h) desc
            """)
    List<ViewStatsDto> aggregate(LocalDateTime start, LocalDateTime end, List<String> uris);

    // Все запросы статистики за временной интервал
    @Query("""
                select new ru.practicum.stats.dto.ViewStatsDto(h.app, h.uri, count(h))
                from Hit h
                where h.timestamp between :start and :end
                group by h.app, h.uri
                order by count(h) desc
            """)
    List<ViewStatsDto> aggregateAll(LocalDateTime start, LocalDateTime end);

    // Уникальные относительно ip запросы статистики по указанным эндпоинтам за временной интервал
    @Query("""
                select new ru.practicum.stats.dto.ViewStatsDto(h.app, h.uri, count(distinct h.ip))
                from Hit h
                where h.timestamp between :start and :end
                    and h.uri in :uris
                group by h.app, h.uri
                order by count(distinct h.ip) desc
            """)
    List<ViewStatsDto> aggregateUnique(LocalDateTime start, LocalDateTime end, List<String> uris);

    // Уникальные относительно ip запросы статистики за временной интервал
    @Query("""
                select new ru.practicum.stats.dto.ViewStatsDto(h.app, h.uri, count(distinct h.ip))
                from Hit h
                where h.timestamp between :start and :end
                group by h.app, h.uri
                order by count(distinct h.ip) desc
            """)
    List<ViewStatsDto> aggregateUniqueAll(LocalDateTime start, LocalDateTime end);
}
