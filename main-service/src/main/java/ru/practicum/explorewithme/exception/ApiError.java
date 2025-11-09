package ru.practicum.explorewithme.exception;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ApiError {

    List<String> errors;
    String status;
    String reason;
    String message;
    String timestamp;
}
