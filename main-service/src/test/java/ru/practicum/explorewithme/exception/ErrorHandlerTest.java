package ru.practicum.explorewithme.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;

class ErrorHandlerTest {

    private final ErrorHandler handler = new ErrorHandler();

    @Test
    void conflict_illegalState() {
        ApiError err = handler.handleIllegalState(new IllegalStateException("conflict"));
        assertThat(err.getStatus()).isEqualTo(HttpStatus.CONFLICT.name());
        assertThat(err.getReason()).isEqualTo("For the requested operation the conditions are not met.");
        assertThat(err.getMessage()).isEqualTo("conflict");
    }

    @Test
    void conflict_integrity() {
        ApiError err = handler.handleIntegrity(new DataIntegrityViolationException("dup"));
        assertThat(err.getStatus()).isEqualTo(HttpStatus.CONFLICT.name());
        assertThat(err.getReason()).isEqualTo("Integrity constraint has been violated.");
    }

    @Test
    void badRequest_illegalArgument() {
        ApiError err = handler.handleOthersBadRequests(new IllegalArgumentException("bad"));
        assertThat(err.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.name());
        assertThat(err.getReason()).isEqualTo("Incorrectly made request.");
    }

    @Test
    void notFound_entity() {
        ApiError err = handler.handleEntityNotFound(new EntityNotFoundException("nf"));
        assertThat(err.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.name());
        assertThat(err.getReason()).isEqualTo("The required object was not found.");
    }

    @Test
    void internal_any() {
        ApiError err = handler.handleInternalError(new RuntimeException("ise"));
        assertThat(err.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.name());
        assertThat(err.getReason()).isEqualTo("Internal server error.");
    }
}
