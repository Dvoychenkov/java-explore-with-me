package ru.practicum.explorewithme.service.publicapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.explorewithme.domain.compilation.Compilation;
import ru.practicum.explorewithme.domain.compilation.CompilationRepository;
import ru.practicum.explorewithme.domain.request.ParticipationRequestRepository;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.mapper.CompilationMapper;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicCompilationServiceImplTest {

    @Mock
    private CompilationRepository repository;

    @Mock
    private ParticipationRequestRepository participationRequestRepository;

    @Spy
    private final CompilationMapper mapper = Mappers.getMapper(CompilationMapper.class);

    @InjectMocks
    private PublicCompilationServiceImpl service;

    @Test
    void find_returnsPage() {
        Compilation c = new Compilation();
        c.setId(1L);
        c.setTitle("t");
        c.setPinned(Boolean.FALSE);
        c.setEvents(new HashSet<>());
        when(repository.findAllByPinned(any(Boolean.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(c)));

        List<CompilationDto> out = service.findAll(Boolean.FALSE, 0, 10);

        assertThat(out).hasSize(1);
        assertThat(out.get(0).getTitle()).isEqualTo("t");
    }

    @Test
    void getById_ok() {
        Compilation c = new Compilation();
        c.setId(2L);
        c.setTitle("x");
        c.setPinned(Boolean.TRUE);
        c.setEvents(Set.of());
        when(repository.findById(2L)).thenReturn(Optional.of(c));

        CompilationDto out = service.getById(2L);
        assertThat(out.getId()).isEqualTo(2L);
        assertThat(out.getPinned()).isTrue();
    }
}
