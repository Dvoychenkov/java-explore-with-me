package ru.practicum.explorewithme.service.publicapi;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.explorewithme.domain.comment.Comment;
import ru.practicum.explorewithme.domain.comment.CommentRepository;
import ru.practicum.explorewithme.domain.comment.CommentStatus;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.domain.event.EventState;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.mapper.CommentMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicCommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private EventRepository eventRepository;

    @Spy
    private final CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

    @InjectMocks
    PublicCommentServiceImpl service;

    @Test
    void listByEvent_okOnlyPublished() {
        Event e = new Event();
        e.setId(1L);
        e.setState(EventState.PUBLISHED);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(e));

        Comment c = new Comment();
        c.setId(100L);
        c.setEvent(e);
        c.setStatus(CommentStatus.PUBLISHED);
        when(commentRepository.findPublishedByEvent(eq(1L), any(CommentStatus.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(c)));

        List<CommentDto> out = service.listByEvent(1L, 0, 10);

        assertThat(out).hasSize(1);
        assertThat(out.get(0).getId()).isEqualTo(100L);
    }

    @Test
    void listByEvent_unpublishedEvent_404() {
        Event e = new Event();
        e.setId(2L);
        e.setState(EventState.PENDING);

        when(eventRepository.findById(2L)).thenReturn(Optional.of(e));

        assertThatThrownBy(() -> service.listByEvent(2L, 0, 10))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
