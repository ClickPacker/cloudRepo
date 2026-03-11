package ru.nikita.cloudrepo.service;

import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.nikita.cloudrepo.dto.response.DirectoryResponseDto;
import ru.nikita.cloudrepo.dto.response.FileResponseDto;
import ru.nikita.cloudrepo.dto.response.ResourceResponseDto;
import ru.nikita.cloudrepo.entity.DirectoryResource;
import ru.nikita.cloudrepo.entity.FileResource;
import ru.nikita.cloudrepo.entity.Resource;
import ru.nikita.cloudrepo.entity.enums.ResourceType;
import ru.nikita.cloudrepo.exception.BadRequestException;
import ru.nikita.cloudrepo.exception.ConflictException;
import ru.nikita.cloudrepo.exception.NotFoundException;
import ru.nikita.cloudrepo.service.impl.StorageService;
import ru.nikita.cloudrepo.utils.download.DownloadUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static ru.nikita.cloudrepo.service.ResourceUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageServiceImpl implements StorageService {
    private final MinioClient minioClient;

    @Override
    public final String getBucketIfExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
            }
            return bucketName;
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    @Override
    public final ResourceResponseDto getResource(Resource resource) {
        if (resource.isDir()) {
            ensureDirectoryExists((DirectoryResource) resource);
            return new DirectoryResponseDto(resource.getResourcePath());
        }
        StatObjectResponse object = getStatFileResource((FileResource) resource);
        return new FileResponseDto(resource.getResourcePath(), object.size());
    }

    @Override
    public void uploadResource(DirectoryResource directory, MultipartFile file) {
        log.info("Uploading resource bucketName={} basePath={}", directory.getBucket(), directory.getResourcePath());
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new BadRequestException("Invalid request body");
        }

        String sanitizedFilename = sanitizeUploadedPath(file.getOriginalFilename());
        String targetPath = directory.isRootDirectory()
                ? sanitizedFilename
                : directory.getResourcePath() + sanitizedFilename;
        FileResource targetResource = new FileResource(targetPath, directory.getBucket());

        if (exists(targetResource)) {
            throw new ConflictException("Resource already exists");
        }
        ensureParentDirectoryExists(targetResource);

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(targetResource.getBucket())
                            .object(targetResource.getResourcePath())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    @Override
    public void deleteResource(Resource resource) {
        log.info("Deleting resource bucketName={} path={}", resource.getBucket(), resource.getResourcePath());
        if (resource.isDir()) {
            delete((DirectoryResource) resource);
            return;
        }
        delete((FileResource) resource);
    }

    @Override
    public byte[] downloadResource(Resource resource) {
        log.info("Downloading resource bucketName={} path={}", resource.getBucket(), resource.getResourcePath());

        boolean isDirectory = resource.isDir();
        List<Item> objects = List.of();

        if (isDirectory) {
            DirectoryResource directory = (DirectoryResource) resource;
            ensureDirectoryExists(directory);
            objects = getObjectList(directory.getBucket(), toPrefix(directory.getResourcePath()), true);
        } else {
            getStatFileResource((FileResource) resource);
        }

        return zipToBytes(resource, isDirectory, objects);
    }

    @Override
    public ResourceResponseDto moveResource(Resource source, Resource destination) {
        log.info("Moving resource bucketName={} from={} to={}", source.getBucket(), source.getResourcePath(), destination.getResourcePath());
        if (!source.compareTypes(destination)) {
            throw new BadRequestException("Invalid path");
        }
        if (source.getResourcePath().equals(destination.getResourcePath())) {
            return getResource(source);
        }

        if (source.isDir()) {
            move((DirectoryResource) source, (DirectoryResource) destination);
        } else {
            move((FileResource) source, (FileResource) destination);
        }
        return getResource(destination);
    }

    @Override
    public List<ResourceResponseDto> searchResources(String bucketName, String query) {
        log.info("Searching resources bucketName={} query={}", bucketName, query);
        String normalizedQuery = validateSearchQuery(query).toLowerCase(Locale.ROOT);
        List<Item> allObjects = getObjectList(bucketName, "", true);
        Map<String, ResourceResponseDto> result = new LinkedHashMap<>();

        allObjects.stream()
                .filter(item -> !(item.objectName().endsWith("/") && item.size() == 0))
                .forEach(item -> addSearchResult(bucketName, normalizedQuery, result, item.objectName()));
        return new ArrayList<>(result.values());
    }

    @Override
    public void createDirectory(DirectoryResource resource) {
        log.info("Creating directory bucketName={} path={}", resource.getBucket(), resource.getResourcePath());
        if (exists(resource)) {
            throw new ConflictException("Directory already exists");
        }
        ensureParentDirectoryExists(resource);
        putEmptyDirectoryObject(resource);
    }

    @Override
    public List<ResourceResponseDto> getDirectoryContent(DirectoryResource resource) {
        if (!exists(resource)) {
            throw new NotFoundException("Resource not found");
        }

        String prefix = toPrefix(resource.getResourcePath());
        return getObjectList(resource.getBucket(), prefix, false)
                .stream()
                .filter(item -> !isCurrentDirectoryMarker(item, prefix))
                .map(item -> item.isDir()
                        ? new DirectoryResponseDto(item.objectName())
                        : new FileResponseDto(item.objectName(), item.size()))
                .toList();
    }

    private StatObjectResponse getStatFileResource(FileResource resource) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(resource.getBucket())
                            .object(resource.getResourcePath())
                            .build()
            );
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    private boolean exists(FileResource resource) {
        try {
            getStatFileResource(resource);
            return true;
        } catch (NotFoundException notFoundException) {
            return false;
        }
    }

    private boolean exists(DirectoryResource resource) {
        if (resource.isRootDirectory()) {
            return true;
        }
        return !getObjectList(resource.getBucket(), toPrefix(resource.getResourcePath()), true).isEmpty();
    }

    private void ensureDirectoryExists(DirectoryResource resource) {
        if (!exists(resource)) {
            throw new NotFoundException("Resource not found");
        }
    }

    private void ensureParentDirectoryExists(Resource resource) {
        String parentPath = resource.getParentPath();
        if (parentPath.isEmpty()) {
            return;
        }
        DirectoryResource parent = new DirectoryResource(
                formatTo(parentPath, ResourceType.DIRECTORY),
                resource.getBucket()
        );
        if (!exists(parent)) {
            throw new NotFoundException("Parent directory does not exist");
        }
    }

    private void ensureParentDirectoryForMove(Resource resource) {
        String parentPath = resource.getParentPath();
        if (parentPath.isEmpty()) {
            return;
        }
        DirectoryResource parent = new DirectoryResource(
                formatTo(parentPath, ResourceType.DIRECTORY),
                resource.getBucket()
        );
        createDirectoryIfMissing(parent);
    }

    private void createDirectoryIfMissing(DirectoryResource directory) {
        if (directory.isRootDirectory() || exists(directory)) {
            return;
        }
        String parentPath = directory.getParentPath();
        if (!parentPath.isEmpty()) {
            DirectoryResource parent = new DirectoryResource(
                    formatTo(parentPath, ResourceType.DIRECTORY),
                    directory.getBucket()
            );
            createDirectoryIfMissing(parent);
        }
        putEmptyDirectoryObject(directory);
    }

    private void putEmptyDirectoryObject(DirectoryResource directory) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(directory.getBucket())
                            .object(directory.getResourcePath())
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build()
            );
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    private void move(FileResource source, FileResource destination) {
        getStatFileResource(source);
        if (exists(destination)) {
            throw new ConflictException("Resource already exists");
        }
        ensureParentDirectoryForMove(destination);

        copyObject(source.getBucket(), source.getResourcePath(), destination.getResourcePath());
        removeObject(source.getBucket(), source.getResourcePath());
    }

    private void move(DirectoryResource source, DirectoryResource destination) {
        ensureDirectoryExists(source);
        if (exists(destination)) {
            throw new ConflictException("Resource already exists");
        }
        ensureParentDirectoryForMove(destination);

        String sourcePrefix = toPrefix(source.getResourcePath());
        String destinationPrefix = toPrefix(destination.getResourcePath());

        getObjectList(source.getBucket(), sourcePrefix, true).stream()
                .map(Item::objectName)
                .forEach(item -> {
                    String relativePath = sourcePrefix.isEmpty()
                            ? item
                            : item.substring(sourcePrefix.length());
                    String destinationItem = destinationPrefix + relativePath;
                    copyObject(source.getBucket(), item, destinationItem);
                    removeObject(source.getBucket(), item);
                });
    }

    private void delete(FileResource resource) {
        getStatFileResource(resource);
        removeObject(resource.getBucket(), resource.getResourcePath());
    }

    private void delete(DirectoryResource resource) {
        ensureDirectoryExists(resource);
        getObjectList(resource.getBucket(), toPrefix(resource.getResourcePath()), true).stream()
                .map(Item::objectName)
                .forEach(objectName -> removeObject(resource.getBucket(), objectName));
    }

    private void copyObject(String bucketName, String sourceObject, String destinationObject) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(destinationObject)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucketName)
                                            .object(sourceObject)
                                            .build()
                            )
                            .build()
            );
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    private void removeObject(String bucketName, String objectPath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .build()
            );
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    private List<Item> getObjectList(String bucketName, String prefix, boolean recursive) {
        try {
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .recursive(recursive)
                            .build()
            );
            return StreamSupport.stream(objects.spliterator(), false)
                    .map(this::readItem)
                    .toList();
        } catch (RuntimeException exception) {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            throw mapStorageException(toException(cause));
        } catch (Exception exception) {
            throw mapStorageException(exception);
        }
    }

    private void addSearchResult(String bucketName,
                                 String query,
                                 Map<String, ResourceResponseDto> result,
                                 String objectPath) {
        Resource objectResource = parse(bucketName, objectPath);
        if (!objectResource.getResourceName().toLowerCase(Locale.ROOT).contains(query)) {
            addParentDirectories(bucketName, objectPath, query, result);
            return;
        }

        result.putIfAbsent(objectPath, getResource(objectResource));
        addParentDirectories(bucketName, objectPath, query, result);
    }

    private void addParentDirectories(String bucketName,
                                      String objectPath,
                                      String query,
                                      Map<String, ResourceResponseDto> result) {
        parentDirectories(objectPath)
                .map(parentPath -> new DirectoryResource(parentPath, bucketName))
                .filter(parent -> parent.getResourceName().toLowerCase(Locale.ROOT).contains(query))
                .forEach(parent -> result.putIfAbsent(parent.getResourcePath(), getResource(parent)));
    }

    private boolean isCurrentDirectoryMarker(Item item, String prefix) {
        return !prefix.isEmpty() && item.objectName().equals(prefix);
    }

    private byte[] zipToBytes(Resource resource, boolean isDirectory, List<Item> objects) {
        try {
            return DownloadUtils.zipResourceToBytes(
                    minioClient,
                    resource,
                    isDirectory,
                    objects
            );
        } catch (RuntimeException exception) {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            throw mapStorageException(toException(cause));
        }
    }

    private Stream<String> parentDirectories(String objectPath) {
        String normalized = objectPath.endsWith("/") && objectPath.length() > 1
                ? objectPath.substring(0, objectPath.length() - 1)
                : objectPath;

        return IntStream.range(0, normalized.length())
                .filter(index -> normalized.charAt(index) == '/')
                .filter(index -> index > 0)
                .mapToObj(index -> normalized.substring(0, index + 1));
    }

    private Item readItem(Result<Item> result) {
        try {
            return result.get();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private Exception toException(Throwable throwable) {
        if (throwable instanceof Exception exception) {
            return exception;
        }
        return new RuntimeException(throwable);
    }

    private RuntimeException mapStorageException(Exception exception) {
        log.error("Storage operation failed: {}", exception.getMessage(), exception);
        if (!(exception instanceof ErrorResponseException errorResponseException)) {
            return new RuntimeException(exception);
        }
        return mapErrorCode(errorResponseException.errorResponse().code(), exception);
    }

    private RuntimeException mapErrorCode(String code, Exception exception) {
        if ("NoSuchKey".equals(code) || "NoSuchBucket".equals(code)) {
            return new NotFoundException("Resource not found");
        }
        if ("PreconditionFailed".equals(code) || "XMinioInvalidObjectName".equals(code)) {
            return new BadRequestException("Invalid path");
        }
        return new RuntimeException(exception);
    }
}
