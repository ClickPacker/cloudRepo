package ru.nikita.cloudrepo.service;

import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.mock.web.MockMultipartFile;
import ru.nikita.cloudrepo.dto.response.FileResponseDto;
import ru.nikita.cloudrepo.dto.response.ResourceResponseDto;
import ru.nikita.cloudrepo.entity.DirectoryResource;
import ru.nikita.cloudrepo.entity.FileResource;
import ru.nikita.cloudrepo.exception.NotFoundException;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers(disabledWithoutDocker = true)
class StorageServiceMinioTestcontainersTest {

    private static final String MINIO_USER = "minioadmin";
    private static final String MINIO_PASSWORD = "minioadmin";

    @Container
    static final GenericContainer<?> minio = new GenericContainer<>(DockerImageName.parse("minio/minio:latest"))
            .withEnv("MINIO_ROOT_USER", MINIO_USER)
            .withEnv("MINIO_ROOT_PASSWORD", MINIO_PASSWORD)
            .withCommand("server", "/data")
            .withExposedPorts(9000)
            .waitingFor(Wait.forHttp("/minio/health/ready").forPort(9000).forStatusCode(200));

    private StorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint("http://%s:%d".formatted(minio.getHost(), minio.getMappedPort(9000)))
                .credentials(MINIO_USER, MINIO_PASSWORD)
                .build();
        storageService = new StorageServiceImpl(minioClient);
    }

    @Test
    void createUploadListAndDownloadFile() {
        String bucket = randomBucketName();
        storageService.getBucketIfExists(bucket);

        DirectoryResource directory = new DirectoryResource("docs/", bucket);
        storageService.createDirectory(directory);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                "text/plain",
                "hello world".getBytes(StandardCharsets.UTF_8)
        );
        storageService.uploadResource(directory, file);

        List<ResourceResponseDto> directoryContent = storageService.getDirectoryContent(directory);
        assertEquals(1, directoryContent.size());

        FileResponseDto uploaded = assertInstanceOf(FileResponseDto.class, directoryContent.get(0));
        assertEquals("docs/", uploaded.getPath());
        assertEquals("hello.txt", uploaded.getName());
        assertEquals(11L, uploaded.getSize());

        byte[] zipBytes = storageService.downloadResource(new FileResource("docs/hello.txt", bucket));
        assertZipContains(zipBytes, "hello.txt", "hello world");
    }

    @Test
    void moveFileCreatesMissingDestinationDirectory() {
        String bucket = randomBucketName();
        storageService.getBucketIfExists(bucket);

        DirectoryResource sourceDirectory = new DirectoryResource("docs/", bucket);
        storageService.createDirectory(sourceDirectory);
        storageService.uploadResource(
                sourceDirectory,
                new MockMultipartFile("file", "notes.txt", "text/plain", "notes".getBytes(StandardCharsets.UTF_8))
        );

        ResourceResponseDto moved = storageService.moveResource(
                new FileResource("docs/notes.txt", bucket),
                new FileResource("archive/notes.txt", bucket)
        );

        FileResponseDto movedFile = assertInstanceOf(FileResponseDto.class, moved);
        assertEquals("archive/", movedFile.getPath());
        assertEquals("notes.txt", movedFile.getName());
        assertThrows(NotFoundException.class, () -> storageService.getResource(new FileResource("docs/notes.txt", bucket)));
    }

    private String randomBucketName() {
        return "test-" + UUID.randomUUID().toString().replace("-", "");
    }

    private void assertZipContains(byte[] zipBytes, String entryName, String expectedContent) {
        try (ZipInputStream zis = new ZipInputStream(new java.io.ByteArrayInputStream(zipBytes))) {
            var entry = zis.getNextEntry();
            while (entry != null) {
                if (entryName.equals(entry.getName())) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    zis.transferTo(buffer);
                    assertEquals(expectedContent, buffer.toString(StandardCharsets.UTF_8));
                    return;
                }
                entry = zis.getNextEntry();
            }
            fail("Zip entry '%s' not found".formatted(entryName));
        } catch (Exception exception) {
            fail("Failed to read zip content: " + exception.getMessage());
        }
    }
}

