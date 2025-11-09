package ru.practicum.explorewithme.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import ru.practicum.explorewithme.domain.compilation.Compilation;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = EventMapper.class
)
public interface CompilationMapper {

    CompilationDto toDto(Compilation compilation);

    @AfterMapping
    default void fillEvents(Compilation compilation, @MappingTarget CompilationDto compilationDto, EventMapper eventMapper) {
        List<EventShortDto> list = compilation.getEvents()
                .stream()
                .map(eventMapper::toShortDto)
                .toList();
        compilationDto.setEvents(list);
    }
}
