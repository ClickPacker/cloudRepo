package ru.nikita.cloudrepo.advice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import ru.nikita.cloudrepo.exception.BadRequestException;
import ru.nikita.cloudrepo.exception.ConflictException;
import ru.nikita.cloudrepo.exception.ExceptionDto;
import ru.nikita.cloudrepo.exception.IncorrectPasswordException;
import ru.nikita.cloudrepo.exception.NotFoundException;
import ru.nikita.cloudrepo.exception.UserDoesNotCreatedException;
import ru.nikita.cloudrepo.exception.UserNotFoundException;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler({UserDoesNotCreatedException.class, IncorrectPasswordException.class, UserNotFoundException.class})
    private ResponseEntity<ExceptionDto> handleUnauthorized(RuntimeException exception) {
        log.warn("Unauthorized request: {}", exception.getMessage());
        return error(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    private ResponseEntity<ExceptionDto> handleConflict(ConflictException exception) {
        log.warn("Conflict: {}", exception.getMessage());
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    private ResponseEntity<ExceptionDto> handleDataConflict(DataIntegrityViolationException exception) {
        log.warn("Data integrity violation: {}", exception.getMessage());
        return error(HttpStatus.CONFLICT, "Resource already exists");
    }

    @ExceptionHandler(NotFoundException.class)
    private ResponseEntity<ExceptionDto> handleNotFound(NotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    private ResponseEntity<ExceptionDto> handleBadRequest(BadRequestException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    private ResponseEntity<ExceptionDto> handleMalformedRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "Invalid request");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<ExceptionDto> handleIllegalArgument(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler({
            BindException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class
    })
    private ResponseEntity<ExceptionDto> handleValidationException(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, resolveValidationMessage(exception));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    private ResponseEntity<ExceptionDto> handleMethodValidationException(HandlerMethodValidationException exception) {
        return error(HttpStatus.BAD_REQUEST, "Invalid path");
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<ExceptionDto> handleException(Exception exception) {
        log.error("Unexpected server error", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private ResponseEntity<ExceptionDto> error(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new ExceptionDto(message));
    }

    private String resolveValidationMessage(Exception exception) {
        if (exception instanceof BindException bindException
                && bindException.getBindingResult().hasFieldErrors()) {
            return bindException.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException
                && methodArgumentNotValidException.getBindingResult().hasFieldErrors()) {
            return methodArgumentNotValidException.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        if (exception instanceof ConstraintViolationException constraintViolationException
                && !constraintViolationException.getConstraintViolations().isEmpty()) {
            return constraintViolationException.getConstraintViolations().iterator().next().getMessage();
        }
        return "Invalid request";
    }
}
