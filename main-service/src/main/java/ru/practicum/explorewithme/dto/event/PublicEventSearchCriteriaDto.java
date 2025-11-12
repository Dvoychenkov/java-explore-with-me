package ru.practicum.explorewithme.dto.event;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.explorewithme.util.DateTimeUtils;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicEventSearchCriteriaDto {

    private String text;
    private List<Long> categories;
    private Boolean paid;

    @DateTimeFormat(pattern = DateTimeUtils.ISO_DATE_TIME_FORMAT)
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = DateTimeUtils.ISO_DATE_TIME_FORMAT)
    private LocalDateTime rangeEnd;

    private Boolean onlyAvailable = false;

    private EventSort sort;

    @Min(0)
    private Integer from = 0;

    @Min(1)
    @Max(1000)
    private Integer size = 10;
}