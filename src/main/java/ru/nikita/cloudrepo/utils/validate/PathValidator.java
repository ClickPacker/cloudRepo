package ru.nikita.cloudrepo.utils.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PathValidator implements ConstraintValidator<IsPath, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String normalized = value.replace('\\', '/');
        if (normalized.contains("..")) {
            return false;
        }

        if ("/".equals(normalized)) {
            return true;
        }

        return normalized.endsWith("/") && !normalized.startsWith("/");
    }
}
