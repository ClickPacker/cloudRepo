package ru.nikita.cloudrepo.utils.validate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceUtilsTest {

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

    @Test
    void rejectsBlankAndNullPath() {
        assertFalse(validator.isValid("", null));
        assertFalse(validator.isValid(" ", null));
        assertFalse(validator.isValid(null, null));
    }

    @Test
    void rejectsTraversalAndAbsoluteDirectoryPath() {
        assertFalse(validator.isValid("folder/../secret/", null));
        assertFalse(validator.isValid("/folder/subfolder/", null));
    }

    @Test
    void acceptsWindowsLikeDirectoryPath() {
        assertTrue(validator.isValid("folder\\subfolder\\", null));
    }
}
