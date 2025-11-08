package ru.practicum.explorewithme.mapper;

import org.mapstruct.*;
import ru.practicum.explorewithme.domain.category.Category;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.Location;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.dto.event.*;

// TODO посмотреть на предмет упрощения
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
//    @Mapping(target = "location", expression = "java(toLocationDto(event.getLocation()))")
    EventFullDto toFullDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", expression = "java(category)")
    @Mapping(target = "initiator", expression = "java(initiator)")
    @Mapping(target = "location", ignore = true)
//    @Mapping(target = "location", expression = "java(toLocationEntity(newEventDto.getLocation()))")
    @Mapping(target = "state", ignore = true)         // состояние меняем вне маппера
    @Mapping(target = "createdOn", ignore = true)     // дату-время создания меняем вне маппера
    @Mapping(target = "publishedOn", ignore = true)   // дату-время публикации меняем вне маппера
    Event toEntity(NewEventDto newEventDto, User initiator, Category category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "eventDate", ignore = true) // Дату евента меняем вне маппера
    @Mapping(target = "category", ignore = true) // категорию меняем вне маппера
    @Mapping(target = "location", ignore = true) // локацию меняем вне маппера
    void updateEntity(UpdateEventUserRequest updateEventUserRequest, @MappingTarget Event entity);

    default LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }

        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }

    default Location toLocationEntity(LocationDto locationDto) {
        if (locationDto == null) {
            return null;
        }

        Location location = new Location();
        location.setLat(locationDto.getLat());
        location.setLon(locationDto.getLon());
        return location;
    }

    @AfterMapping
    default void fillLocation(Event event, @MappingTarget EventFullDto eventFullDto) {
        eventFullDto.setLocation(toLocationDto(event.getLocation()));
    }

    @AfterMapping
    default void fillLocation(EventFullDto eventFullDto, @MappingTarget Event event) {
        event.setLocation(toLocationEntity(eventFullDto.getLocation()));
    }
}