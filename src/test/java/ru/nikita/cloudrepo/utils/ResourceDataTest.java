package ru.nikita.cloudrepo.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceDataTest {

    @Test
    void getNameReturnsRootForRootPath() {
        assertEquals("root", new ResourceData("/").getName());
    }

    @Test
    void getNameReturnsLastDirectorySegment() {
        assertEquals("archive", new ResourceData("docs/archive/").getName());
    }

    @Test
    void getNameReturnsFilenameForFilePath() {
        assertEquals("report.pdf", new ResourceData("docs/report.pdf").getName());
    }

    @Test
    void getNameNormalizesWindowsSeparators() {
        assertEquals("photo.png", new ResourceData("images\\photo.png").getName());
    }
}

