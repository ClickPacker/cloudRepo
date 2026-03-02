package ru.nikita.cloudrepo.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nikita.cloudrepo.exception.ExceptionDto;
import ru.nikita.cloudrepo.exception.IncorrectPasswordException;
import ru.nikita.cloudrepo.exception.UserDoesNotCreatedException;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(UserDoesNotCreatedException.class)
    private ResponseEntity<ExceptionDto> userDoesNotCreatedHandler(UserDoesNotCreatedException exception) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionDto(exception.getMessage()));
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    private ResponseEntity<ExceptionDto> incorrectPasswordHandler(IncorrectPasswordException exception) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionDto(exception.getMessage()));
    }
}
