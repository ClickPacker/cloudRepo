package ru.nikita.cloudrepo.exception;

public class UserDoesNotCreatedException extends RuntimeException {
    public UserDoesNotCreatedException(String message) {
        super(message);
    }
}
