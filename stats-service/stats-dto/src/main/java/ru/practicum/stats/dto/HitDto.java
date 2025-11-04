package ru.practicum.stats.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class HitDto {

    Long id;
    String app;
    String uri;
    String ip;
    String timestamp;
}