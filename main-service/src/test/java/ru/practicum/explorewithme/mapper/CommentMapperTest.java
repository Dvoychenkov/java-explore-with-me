package ru.practicum.explorewithme.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.explorewithme.domain.comment.Comment;
import ru.practicum.explorewithme.domain.comment.CommentStatus;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.Location;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.dto.comment.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.explorewithme.domain.comment.CommentStatus.PUBLISHED;

class CommentMapperTest {

    private final CommentMapper mapper = Mappers.getMapper(CommentMapper.class);

    @Test
    void toDto_mapsAllFields() {
        User author = new User();
        author.setId(10L);
        author.setName("Alice");
        author.setEmail("a@a.com");

        Event event = new Event();
        event.setId(20L);
        Location loc = new Location();
        loc.setLat(1.1);
        loc.setLon(2.2);
        event.setLocation(loc);

        Comment c = new Comment();
        c.setId(100L);
        c.setAuthor(author);
        c.setEvent(event);
        c.setText("Hello");
        c.setStatus(PUBLISHED);
        c.setCreatedOn(LocalDateTime.now().minusHours(1));
        c.setUpdatedOn(LocalDateTime.now());

        CommentDto dto = mapper.toDto(c);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getAuthorId()).isEqualTo(10L);
        assertThat(dto.getAuthorName()).isEqualTo("Alice");
        assertThat(dto.getEventId()).isEqualTo(20L);
        assertThat(dto.getText()).isEqualTo("Hello");
        assertThat(dto.getStatus()).isEqualTo(PUBLISHED);
        assertThat(dto.getCreatedOn()).isNotNull();
    }
}
