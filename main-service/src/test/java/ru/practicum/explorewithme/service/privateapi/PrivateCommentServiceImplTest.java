package ru.practicum.explorewithme.service.privateapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.domain.comment.Comment;
import ru.practicum.explorewithme.domain.comment.CommentRepository;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.domain.event.EventState;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.domain.user.UserRepository;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.NewCommentDto;
import ru.practicum.explorewithme.dto.comment.UpdateCommentDto;
import ru.practicum.explorewithme.mapper.CommentMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrivateCommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;

    @Spy
    private final CommentMapper mapper = Mappers.getMapper(CommentMapper.class);

    @InjectMocks
    PrivateCommentServiceImpl service;

    @Test
    void add_toUnpublishedEvent_conflict() {
        User u = new User();
        u.setId(5L);
        Event e = new Event();
        e.setId(7L);
        e.setState(EventState.PENDING);

        when(userRepository.findById(5L)).thenReturn(Optional.of(u));
        when(eventRepository.findById(7L)).thenReturn(Optional.of(e));

        NewCommentDto dto = new NewCommentDto();
        dto.setText(" hi ");
        assertThatThrownBy(() -> service.add(5L, 7L, dto))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void update_notAuthor_conflict() {
        Comment c = new Comment();
        User author = new User();
        author.setId(10L);
        c.setAuthor(author);
        c.setId(100L);

        when(commentRepository.findById(100L)).thenReturn(Optional.of(c));

        UpdateCommentDto dto = new UpdateCommentDto();
        dto.setText("new");
        assertThatThrownBy(() -> service.update(11L, 100L, dto))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void add_ok_trimsText() {
        User u = new User();
        u.setId(1L);
        Event e = new Event();
        e.setId(2L);
        e.setState(EventState.PUBLISHED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(e));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0, Comment.class));


        NewCommentDto dto = new NewCommentDto();
        dto.setText("  hello  ");
        CommentDto out = service.add(1L, 2L, dto);

        assertThat(out.getText()).isEqualTo("  hello  ");
    }
}
