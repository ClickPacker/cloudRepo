package ru.nikita.cloudrepo.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileDataTest {
    @Test
    void extractsFileName() {
        ResourceData resourceData = new ResourceData("folder1/folder2/file.txt");
        assertEquals("file.txt", resourceData.getName());
    }

    @Test
    void extractsParentPathWithNormalizedSeparators() {
        ResourceData resourceData = new ResourceData("folder1\\folder2\\file.txt");
        assertEquals("folder1/folder2/", resourceData.getPathToFile());
    }
}
