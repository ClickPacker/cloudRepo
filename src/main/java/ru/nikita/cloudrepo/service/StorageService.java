package ru.nikita.cloudrepo.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.nikita.cloudrepo.config.repository.MinioConfig;
import ru.nikita.cloudrepo.dto.response.DirectoryResponseDto;
import ru.nikita.cloudrepo.dto.response.ObjectResponseDto;
import ru.nikita.cloudrepo.dto.response.ResourceResponseDto;
import ru.nikita.cloudrepo.exception.BadRequestException;
import ru.nikita.cloudrepo.exception.ConflictException;
import ru.nikita.cloudrepo.exception.NotFoundException;
import ru.nikita.cloudrepo.utils.ResourceData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static ru.nikita.cloudrepo.service.PathFormatter.parentPath;
import static ru.nikita.cloudrepo.service.PathFormatter.sanitizeUploadedPath;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    private RuntimeException mapStorageException(Exception exception) {
        log.error("Storage operation failed: {}", exception.getMessage(), exception);
        if (exception instanceof ErrorResponseException errorResponseException) {
            String code = errorResponseException.errorResponse().code();
            if ("NoSuchKey".equals(code) || "NoSuchBucket".equals(code)) {
                return new NotFoundException("Resource not found");
            }
            if ("PreconditionFailed".equals(code) || "XMinioInvalidObjectName".equals(code)) {
                return new BadRequestException("Invalid path");
            }
        }
        return new RuntimeException(exception);
    }

    public void createBucket(Long id) {
        String bucketName = minioConfig.getBucketPattern().formatted(id);
        log.info("Creating bucket for userId={} bucket={}", id, bucketName);
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    private String getBucketName(Long id) {
        String bucketName = minioConfig.getBucketPattern().formatted(id);
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            return bucketName;
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    private String validateResourcePath(String path) {
        if (path == null || path.isBlank()) {
            throw new BadRequestException("Invalid path");
        }
        String normalized = path.replace('\\', '/');
        if (normalized.contains("..") || normalized.startsWith("/")) {
            throw new BadRequestException("Invalid path");
        }
        return normalized;
    }

    private String validateDirectoryPath(String path) {
        if (path == null || path.isBlank()) {
            throw new BadRequestException("Invalid path");
        }
        String normalized = path.replace('\\', '/');
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        if (normalized.contains("..") || (normalized.startsWith("/") && !normalized.equals("/"))) {
            throw new BadRequestException("Invalid path");
        }
        return normalized;
    }

    private String validateSearchQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }
        String normalized = query.replace('\\', '/');
        if (normalized.contains("..") || normalized.startsWith("/")) {
            throw new BadRequestException("Invalid path");
        }
        return normalized;
    }

    private boolean directoryExists(Long userId, String path) {
        if ("/".equals(path)) {
            return true;
        }
        try {
            String bucketName = getBucketName(userId);
            String prefix = path.startsWith("/") ? path.substring(1) : path;
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .recursive(true)
                            .build()
            );
            for (Result<Item> result : objects) {
                result.get();
                return true;
            }
            return false;
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    private boolean exists(Long userId, String path) {
        if (path.endsWith("/")) {
            return directoryExists(userId, path);
        }
        try {
            getResource(userId, path);
            return true;
        } catch (Exception e) {
            if (e instanceof NotFoundException) {
                return false;
            }
            throw e;
        }
    }

    private StatObjectResponse statObject(Long userId, String path) {
        try {
            String bucketName = getBucketName(userId);
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    private byte[] readObject(String bucketName, String objectKey) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .build()
        )) {
            return stream.readAllBytes();
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    private void removeObject(String bucketName, String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    private void copyObject(String bucketName, String from, String to) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(to)
                            .source(CopySource.builder()
                                    .bucket(bucketName)
                                    .object(from)
                                    .build())
                            .build()
            );
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    private List<Item> listObjects(String bucketName, String prefix, boolean recursive) {
        try {
            List<Item> items = new ArrayList<>();
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .recursive(recursive)
                            .build()
            );
            for (Result<Item> result : objects) {
                items.add(result.get());
            }
            return items;
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    public ObjectResponseDto getResource(Long userId, String objectName) {
        String validatedPath = objectName.endsWith("/")
                ? validateDirectoryPath(objectName)
                : validateResourcePath(objectName);

        var bucketName = getBucketName(userId);
        var file = new ResourceData(validatedPath);

        return switch (file.getType()) {
            case FILE -> {
                var object = statObject(userId, validatedPath);
                yield new ResourceResponseDto(file.getPathToFile(), file.getName(), object.size());
            }
            case DIRECTORY -> {
                if (!directoryExists(userId, validatedPath)) {
                    throw new NotFoundException("Resource not found");
                }
                yield new DirectoryResponseDto(file.getPathToFile(), file.getName());
            }
        };
    }

    public void deleteResource(Long userId, String objectName) {
        log.info("Deleting resource userId={} path={}", userId, objectName);
        String validatedPath = objectName.endsWith("/")
                ? validateDirectoryPath(objectName)
                : validateResourcePath(objectName);

        var bucketName = getBucketName(userId);

        if (validatedPath.endsWith("/")) {
            if (!directoryExists(userId, validatedPath)) {
                throw new NotFoundException("Resource not found");
            }
            List<Item> objects = listObjects(bucketName, validatedPath, true);
            for (Item item : objects) {
                removeObject(bucketName, item.objectName());
            }
        } else {
            statObject(userId, validatedPath);
            removeObject(bucketName, validatedPath);
        }
    }

    public byte[] downloadResource(Long userId, String objectName) {
        log.info("Downloading resource userId={} path={}", userId, objectName);
        String validatedPath = objectName.endsWith("/")
                ? validateDirectoryPath(objectName)
                : validateResourcePath(objectName);

        var bucketName = getBucketName(userId);

        if (validatedPath.endsWith("/")) {
            if (!directoryExists(userId, validatedPath)) {
                throw new NotFoundException("Resource not found");
            }
            return zipDirectory(bucketName, validatedPath);
        }

        statObject(userId, validatedPath);
        return readObject(bucketName, validatedPath);
    }

    private byte[] zipDirectory(String bucketName, String path) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            List<Item> objects = listObjects(bucketName, path, true);

            for (Item item : objects) {
                if (item.isDir()) continue;

                String relativeName = item.objectName().substring(path.length());
                ZipEntry zipEntry = new ZipEntry(relativeName);
                zos.putNextEntry(zipEntry);
                zos.write(readObject(bucketName, item.objectName()));
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Transactional
    public void moveResource(Long userId, String sourcePath, String destinationPath) {
        log.info("Moving resource userId={} from={} to={}", userId, sourcePath, destinationPath);
        String validatedSource = sourcePath.endsWith("/")
                ? validateDirectoryPath(sourcePath)
                : validateResourcePath(sourcePath);
        String validatedDest = destinationPath.endsWith("/")
                ? validateDirectoryPath(destinationPath)
                : validateResourcePath(destinationPath);

        if (!validatedSource.endsWith("/") && validatedDest.endsWith("/")) {
            throw new BadRequestException("Invalid path");
        }

        if (validatedSource.equals(validatedDest)) {
            return;
        }

        if (validatedSource.endsWith("/")) {
            moveDirectory(userId, validatedSource, validatedDest);
        } else {
            moveFile(userId, validatedSource, validatedDest);
        }
    }

    private void moveFile(Long userId, String from, String to) {
        var bucketName = getBucketName(userId);
        statObject(userId, from);

        if (exists(userId, to)) {
            throw new ConflictException("Resource already exists");
        }

        copyObject(bucketName, from, to);
        removeObject(bucketName, from);
    }

    private void moveDirectory(Long userId, String fromPrefix, String toPrefix) {
        var bucketName = getBucketName(userId);

        if (!directoryExists(userId, fromPrefix)) {
            throw new NotFoundException("Resource not found");
        }

        if (exists(userId, toPrefix)) {
            throw new ConflictException("Resource already exists");
        }

        List<Item> objects = listObjects(bucketName, fromPrefix, true);
        List<String> sourceKeys = objects.stream().map(Item::objectName).toList();

        for (String sourceKey : sourceKeys) {
            String suffix = sourceKey.substring(fromPrefix.length());
            copyObject(bucketName, sourceKey, toPrefix + suffix);
        }

        for (String sourceKey : sourceKeys) {
            removeObject(bucketName, sourceKey);
        }
    }

    public List<ObjectResponseDto> search(Long userId, String query) {
        log.info("Searching resources userId={} query={}", userId, query);
        String normalizedQuery = validateSearchQuery(query).toLowerCase();
        var bucketName = getBucketName(userId);

        Map<String, ObjectResponseDto> result = new LinkedHashMap<>();
        List<Item> objects = listObjects(bucketName, "", true);

        for (Item item : objects) {
            String objectName = item.objectName();

            if (objectName.endsWith("/") && item.size() == 0) {
                continue;
            }

            ResourceData resourceData = new ResourceData(objectName);
            String name = resourceData.getName();

            if (name.toLowerCase().contains(normalizedQuery)) {
                result.put(objectName, getResource(userId, objectName));
            }

            addParentDirectories(userId, objectName, normalizedQuery, result);
        }

        return new ArrayList<>(result.values());
    }

    private void addParentDirectories(Long userId, String objectName, String query, Map<String, ObjectResponseDto> result) {
        String current = objectName;
        while (current.contains("/")) {
            int slashIndex = current.lastIndexOf('/');
            if (slashIndex <= 0) break;

            String parent = current.substring(0, slashIndex + 1);
            ResourceData parentData = new ResourceData(parent);

            if (parentData.getName().toLowerCase().contains(query)) {
                result.putIfAbsent(parent, getResource(userId, parent));
            }
            current = parent.substring(0, parent.length() - 1);
        }
    }

    public void upload(Long userId, String basePath, MultipartFile file) {
        log.info("Uploading resource userId={} basePath={}", userId, basePath);
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new BadRequestException("Invalid request body");
        }

        String validatedBasePath = validateDirectoryPath(basePath);
        String sanitizedFilename = sanitizeUploadedPath(file.getOriginalFilename());
        String targetPath = validatedBasePath + sanitizedFilename;

        if (exists(userId, targetPath)) {
            throw new ConflictException("Resource already exists");
        }

        String parentDir = parentPath(targetPath);
        if (!parentDir.isEmpty() && !directoryExists(userId, parentDir)) {
            throw new NotFoundException("Parent directory does not exist");
        }

        try {
            var bucketName = getBucketName(userId);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(targetPath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    public void createDirectory(Long userId, String path) {
        log.info("Creating directory userId={} path={}", userId, path);
        String validatedPath = validateDirectoryPath(path);

        if (exists(userId, validatedPath)) {
            throw new ConflictException("Directory already exists");
        }

        String parentDir = parentPath(validatedPath);
        if (!parentDir.isEmpty() && !directoryExists(userId, parentDir)) {
            throw new NotFoundException("Parent directory does not exist");
        }

        try {
            var bucketName = getBucketName(userId);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(validatedPath)
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build()
            );
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }
}
