package ru.practicum.stats.server.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.stats.dto.NewHitDto;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.server.model.Hit;

import static ru.practicum.stats.server.util.DateTimeUtils.ISO_DATE_TIME_FORMAT;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HitMapper {

    @Mapping(target = "timestamp", source = "timestamp", dateFormat = ISO_DATE_TIME_FORMAT)
    @Mapping(target = "id", ignore = true)
    Hit toHit(NewHitDto dto);

    @InheritInverseConfiguration
    HitDto toHitDto(Hit hit);
}
