package ru.practicum.explorewithme.mapper;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.explorewithme.domain.compilation.Compilation;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ExtendWith(MockitoExtension.class)
class CompilationMapperTest {

    private final CompilationMapper compilationMapper;

    @Test
    void compilation_toDto() {
        Event e = new Event();
        e.setId(1L);
        Compilation c = new Compilation();
        c.setId(3L);
        c.setTitle("t");
        c.setPinned(Boolean.TRUE);
        c.setEvents(Set.of(e));
        CompilationDto dto = compilationMapper.toDto(c);
        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getPinned()).isTrue();
    }
}
