package ru.nikita.cloudrepo.service;

import org.junit.jupiter.api.Test;
import ru.nikita.cloudrepo.entity.DirectoryResource;
import ru.nikita.cloudrepo.entity.FileResource;
import ru.nikita.cloudrepo.entity.Resource;
import ru.nikita.cloudrepo.entity.enums.ResourceType;
import ru.nikita.cloudrepo.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceUtilsTest {

    @Test
    void parseBuildsDirectoryForDirectoryPath() {
        Resource resource = ResourceUtils.parse("bucket-1", "folder/subfolder/");

        assertInstanceOf(DirectoryResource.class, resource);
        assertEquals("folder/subfolder/", resource.getResourcePath());
        assertEquals("bucket-1", resource.getBucket());
    }

    @Test
    void parseBuildsFileForFilePath() {
        Resource resource = ResourceUtils.parse("bucket-1", "folder/file.txt");

        assertInstanceOf(FileResource.class, resource);
        assertEquals("folder/file.txt", resource.getResourcePath());
        assertEquals("bucket-1", resource.getBucket());
    }

    @Test
    void formatToAddsTrailingSlashForDirectoryType() {
        String formatted = ResourceUtils.formatTo("folder/subfolder", ResourceType.DIRECTORY);

        assertEquals("folder/subfolder/", formatted);
    }

    @Test
    void formatToRejectsPathTraversal() {
        assertThrows(BadRequestException.class, () -> ResourceUtils.formatTo("../folder", ResourceType.DIRECTORY));
    }

    @Test
    void sanitizeUploadedPathNormalizesSlashesAndRejectsInvalidPath() {
        assertEquals("folder/file.txt", ResourceUtils.sanitizeUploadedPath("folder\\file.txt"));
        assertThrows(BadRequestException.class, () -> ResourceUtils.sanitizeUploadedPath("/file.txt"));
        assertThrows(BadRequestException.class, () -> ResourceUtils.sanitizeUploadedPath("../file.txt"));
    }

    @Test
    void validateSearchQuerySupportsBlankAndRejectsAbsolutePath() {
        assertEquals("", ResourceUtils.validateSearchQuery(null));
        assertEquals("", ResourceUtils.validateSearchQuery(" "));
        assertEquals("folder/file", ResourceUtils.validateSearchQuery("folder/file"));
        assertThrows(BadRequestException.class, () -> ResourceUtils.validateSearchQuery("/folder/file"));
    }

    @Test
    void normalizeDirectoryPathNormalizesInput() {
        assertEquals("/", ResourceUtils.normalizeDirectoryPath(null));
        assertEquals("/", ResourceUtils.normalizeDirectoryPath("   "));
        assertEquals("/", ResourceUtils.normalizeDirectoryPath("/"));
        assertEquals("folder/subfolder/", ResourceUtils.normalizeDirectoryPath("/folder/subfolder"));
        assertEquals("folder/subfolder/", ResourceUtils.normalizeDirectoryPath("folder/subfolder/"));
    }
}

