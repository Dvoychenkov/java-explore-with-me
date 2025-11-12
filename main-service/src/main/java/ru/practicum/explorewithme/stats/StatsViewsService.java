package ru.practicum.explorewithme.stats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface StatsViewsService {

    public Map<String, Long> fetchViews(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
