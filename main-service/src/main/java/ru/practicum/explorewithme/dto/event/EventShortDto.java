package ru.practicum.explorewithme.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.user.UserShortDto;
import ru.practicum.explorewithme.util.DateTimeUtils;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {

    private Long id;
    private String title;
    private String annotation;

    @JsonFormat(pattern = DateTimeUtils.ISO_DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    private Boolean paid;
    private Long views;
    private CategoryDto category;
    private UserShortDto initiator;
    private Long confirmedRequests;
}
