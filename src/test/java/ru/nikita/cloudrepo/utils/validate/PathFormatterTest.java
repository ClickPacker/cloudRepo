package ru.nikita.cloudrepo.utils.validate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathFormatterTest {

    private final PathValidator validator = new PathValidator();

    @Test
    void acceptsRootPath() {
        assertTrue(validator.isValid("/", null));
    }

    @Test
    void acceptsNestedDirectoryPath() {
        assertTrue(validator.isValid("folder/subfolder/", null));
    }

    @Test
    void rejectsPathWithoutTrailingSlash() {
        assertFalse(validator.isValid("folder/subfolder", null));
    }

    @Test
    void rejectsFileLikePath() {
        assertFalse(validator.isValid("folder/file.txt", null));
    }
}
