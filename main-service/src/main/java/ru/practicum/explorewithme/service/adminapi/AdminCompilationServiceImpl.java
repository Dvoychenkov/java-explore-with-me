package ru.practicum.explorewithme.service.adminapi;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.domain.compilation.Compilation;
import ru.practicum.explorewithme.domain.compilation.CompilationRepository;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.NewCompilationDto;
import ru.practicum.explorewithme.dto.compilation.UpdateCompilationRequest;
import ru.practicum.explorewithme.mapper.CompilationMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCompilationServiceImpl implements AdminCompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setPinned(Boolean.TRUE.equals(newCompilationDto.getPinned()));

        List<Long> compilationEvents = newCompilationDto.getEvents();
        if (compilationEvents != null && !compilationEvents.isEmpty()) {
            List<Event> events = eventRepository.findAllById(compilationEvents);
            compilation.setEvents(new HashSet<>(events));
        }

        Compilation saved = compilationRepository.save(compilation);
        return compilationMapper.toDto(saved);
    }

    @Override
    public void delete(Long compilationId) {
        if (!compilationRepository.existsById(compilationId)) {
            throw new EntityNotFoundException("Compilation not found by id: " + compilationId);
        }

        compilationRepository.deleteById(compilationId);
    }

    @Override
    public CompilationDto update(Long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new EntityNotFoundException("Compilation not found by id: " + compilationId));

        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(updateCompilationRequest.getEvents());
            Set<Event> eventSet = new HashSet<>(events);
            compilation.setEvents(eventSet);
        }

        return compilationMapper.toDto(compilationRepository.save(compilation));
    }
}
