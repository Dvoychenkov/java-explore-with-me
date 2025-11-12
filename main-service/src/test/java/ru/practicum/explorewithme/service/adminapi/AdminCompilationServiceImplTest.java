package ru.practicum.explorewithme.service.adminapi;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.domain.compilation.Compilation;
import ru.practicum.explorewithme.domain.compilation.CompilationRepository;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.NewCompilationDto;
import ru.practicum.explorewithme.dto.compilation.UpdateCompilationRequest;
import ru.practicum.explorewithme.mapper.CompilationMapper;
import ru.practicum.explorewithme.mapper.CompilationMapperImpl;
import ru.practicum.explorewithme.mapper.EventMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCompilationServiceImplTest {

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private EventRepository eventRepository;

    private final CompilationMapper mapper = Mappers.getMapper(CompilationMapper.class);

    private AdminCompilationServiceImpl service;

    private CompilationMapperImpl mapperImpl;

    @BeforeEach
    void setUp() throws Exception {
        mapperImpl = new CompilationMapperImpl();
        Field f = CompilationMapperImpl.class.getDeclaredField("eventMapper");
        f.setAccessible(true);
        f.set(mapperImpl, Mappers.getMapper(EventMapper.class));

        service = new AdminCompilationServiceImpl(compilationRepository, eventRepository, mapperImpl);
    }

    @Test
    void create_withEvents_ok() {
        Event e1 = new Event();
        e1.setId(1L);
        when(eventRepository.findAllById(anyList())).thenReturn(List.of(e1));
        when(compilationRepository.save(any(Compilation.class)))
                .thenAnswer(inv -> {
                    Compilation c = inv.getArgument(0, Compilation.class);
                    c.setId(10L);
                    return c;
                });

        NewCompilationDto dto = NewCompilationDto.builder()
                .title("top")
                .pinned(Boolean.TRUE)
                .events(List.of(1L))
                .build();

        CompilationDto out = service.create(dto);

        assertThat(out.getId()).isEqualTo(10L);
        assertThat(out.getTitle()).isEqualTo("top");
        assertThat(out.getPinned()).isTrue();
        assertThat(out.getEvents()).hasSize(1);
    }

    @Test
    void update_changeTitlePinnedAndEvents_ok() {
        Compilation c = new Compilation();
        c.setId(5L);
        c.setTitle("old");
        c.setPinned(Boolean.FALSE);
        c.setEvents(new HashSet<>());
        when(compilationRepository.findById(5L)).thenReturn(Optional.of(c));

        Event e = new Event();
        e.setId(3L);
        when(eventRepository.findAllById(List.of(3L))).thenReturn(List.of(e));
        when(compilationRepository.save(any(Compilation.class))).thenAnswer(inv -> inv.getArgument(0, Compilation.class));

        UpdateCompilationRequest req = UpdateCompilationRequest.builder()
                .title("new")
                .pinned(Boolean.TRUE)
                .events(List.of(3L))
                .build();

        CompilationDto out = service.update(5L, req);

        assertThat(out.getTitle()).isEqualTo("new");
        assertThat(out.getPinned()).isTrue();
        assertThat(out.getEvents()).hasSize(1);
    }

    @Test
    void delete_notFound_throws404() {
        when(compilationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);

        verify(compilationRepository, never()).deleteById(99L);
    }
}
