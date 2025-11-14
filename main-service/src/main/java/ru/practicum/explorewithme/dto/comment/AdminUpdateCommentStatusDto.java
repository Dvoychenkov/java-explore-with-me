package ru.practicum.explorewithme.dto.comment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.domain.comment.CommentStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateCommentStatusDto {

    @NotNull
    private CommentStatus status;
}