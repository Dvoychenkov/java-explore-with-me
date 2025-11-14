package ru.practicum.explorewithme.mapper;

import org.mapstruct.*;
import ru.practicum.explorewithme.domain.comment.Comment;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.NewCommentDto;
import ru.practicum.explorewithme.dto.comment.UpdateCommentDto;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.name")
    @Mapping(target = "createdOn", source = "createdOn")
    @Mapping(target = "updatedOn", source = "updatedOn")
    @Mapping(target = "status", source = "status")
    CommentDto toDto(Comment comment);

    List<CommentDto> toDtoList(List<Comment> comments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", source = "author")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "status", ignore = true)
    Comment toEntity(NewCommentDto newCommentDto, User author, Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateCommentDto updateCommentDto, @MappingTarget Comment entity);
}
