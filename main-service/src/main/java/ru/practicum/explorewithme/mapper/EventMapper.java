package ru.practicum.explorewithme.mapper;

import org.mapstruct.*;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.Location;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.dto.event.LocationDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "initiatorId", source = "initiator.id")
    @Mapping(target = "initiatorName", source = "initiator.name")
    @Mapping(target = "views", ignore = true)
    EventShortDto toShortDto(Event event);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "initiatorId", source = "initiator.id")
    @Mapping(target = "initiatorName", source = "initiator.name")
    @Mapping(target = "state", expression = "java(event.getState().name())")
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "location", ignore = true)
    EventFullDto toFullDto(Event event);

    default LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }

        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }

    @AfterMapping
    default void fillLocation(Event event, @MappingTarget EventFullDto eventFullDto) {
        eventFullDto.setLocation(toLocationDto(event.getLocation()));
    }
}