package ru.practicum.explorewithme.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.explorewithme.util.DateTimeUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIntegrity(DataIntegrityViolationException ex) {
        log.warn("Integrity violation: {}", ex.getMessage());

        ex.getMostSpecificCause();
        return buildApiError(
                HttpStatus.CONFLICT,
                "Integrity constraint has been violated.",
                ex.getMostSpecificCause().getMessage(),
                ex
        );
    }

    // 409 - Conflict
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIllegalState(IllegalStateException ex) {
        log.warn("Conflict: {}", ex.getMessage());

        return buildApiError(
                HttpStatus.CONFLICT,
                "For the requested operation the conditions are not met.",
                ex.getMessage(),
                ex
        );
    }

    // 400 - Bad Request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> String.format("Field: %s.%nError: %s.%nValue: %s",
                        fieldError.getField(), fieldError.getDefaultMessage(), fieldError.getRejectedValue())
                )
                .findFirst()
                .orElse(ex.getMessage());

        log.warn("Bad request, not valid args: {}. {}", message, ex.getMessage());
        return buildApiError(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                message,
                ex
        );
    }

    // 400 - Bad Request
    @ExceptionHandler({
            IllegalArgumentException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleOthersBadRequests(Exception ex) {
        log.warn("Bad request: {}", ex.getMessage());

        return buildApiError(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                ex.getMessage(),
                ex
        );
    }

    // 404 - Not Found
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());

        return buildApiError(
                HttpStatus.NOT_FOUND,
                "The required object was not found.",
                ex.getMessage(),
                ex
        );
    }

    // 500 - Internal Server Error (все остальные исключения)
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInternalError(Exception ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);

        return buildApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error.",
                ex.getMessage(),
                ex
        );
    }

    private ApiError buildApiError(HttpStatus status, String reason, String message, Exception ex) {
        List<String> errorsStackTraceLst = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();

        return ApiError.builder()
                .errors(errorsStackTraceLst)
                .status(status.name())
                .reason(reason)
                .message(message)
                .timestamp(DateTimeUtils.toString(LocalDateTime.now()))
                .build();
    }
}
