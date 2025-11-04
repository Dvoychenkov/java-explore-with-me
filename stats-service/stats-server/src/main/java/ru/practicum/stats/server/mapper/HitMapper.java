package ru.practicum.stats.server.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.stats.dto.NewHitDto;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.server.model.Hit;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HitMapper {
    String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Mapping(target = "timestamp", source = "timestamp", dateFormat = TIMESTAMP_PATTERN)
    @Mapping(target = "id", ignore = true)
    Hit toHit(NewHitDto dto);

    @InheritInverseConfiguration
    HitDto toHitDto(Hit hit);
}
