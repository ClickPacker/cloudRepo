package ru.nikita.cloudrepo.service;

import lombok.experimental.UtilityClass;
import ru.nikita.cloudrepo.entity.DirectoryResource;
import ru.nikita.cloudrepo.entity.FileResource;
import ru.nikita.cloudrepo.entity.Resource;
import ru.nikita.cloudrepo.entity.enums.ResourceType;
import ru.nikita.cloudrepo.exception.BadRequestException;

@UtilityClass
public class ResourceUtils {
    public static Resource parse(String bucket, String resourceName) {
        ResourceType type = getType(resourceName);
        String resourcePath = formatTo(resourceName, type);
        return switch (type) {
            case DIRECTORY -> new DirectoryResource(resourcePath, bucket);
            case FILE -> new FileResource(resourcePath, bucket);
        };
    }

    public static DirectoryResource parseDirectory(String bucket, String resourceName) {
        return new DirectoryResource(formatTo(resourceName, ResourceType.DIRECTORY), bucket);
    }

    public static FileResource parseFile(String bucket, String resourceName) {
        return new FileResource(formatTo(resourceName, ResourceType.FILE), bucket);
    }

    public static ResourceType getType(String resourcePath) {
        String normalized = normalizeAndRequirePath(resourcePath);
        return normalized.endsWith("/") ? ResourceType.DIRECTORY : ResourceType.FILE;
    }

    public static String format(String resourcePath) {
        return formatTo(resourcePath, getType(resourcePath));
    }

    public static String formatTo(String resourcePath, ResourceType type) {
        String normalized = normalizeAndRequirePath(resourcePath);
        validatePathTraversal(normalized);
        validateLeadingSlash(normalized, type);
        return ensureTypePath(normalized, type);
    }

    public static String toPrefix(String path) {
        if ("/".equals(path))
            return "";
        return path.startsWith("/") ? path.substring(1) : path;
    }

    public static String sanitizeUploadedPath(String originalFilename) {
        String normalized = originalFilename.replace('\\', '/');
        if (normalized.isBlank() || normalized.startsWith("/") || normalized.contains("..")) {
            throw new BadRequestException("Invalid request body");
        }
        return normalized;
    }

    public static String validateSearchQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }
        String normalized = query.replace('\\', '/');
        validatePathTraversal(normalized);
        validateAbsolutePath(normalized);
        return normalized;
    }

    public static String fileName(String path) {
        int delimiterIndex = path.lastIndexOf('/');
        return delimiterIndex < 0 ? path : path.substring(delimiterIndex + 1);
    }

    public static String directoryName(String path) {
        if ("/".equals(path)) {
            return "root";
        }
        String clean = path.substring(0, path.length() - 1);
        int delimiterIndex = clean.lastIndexOf('/');
        return delimiterIndex < 0 ? clean : clean.substring(delimiterIndex + 1);
    }

    public static String parentPath(String path) {
        String clean = path.substring(0, path.length() - 1);
        int delimiterIndex = clean.lastIndexOf('/');
        if (delimiterIndex < 0) {
            return "";
        }
        return clean.substring(0, delimiterIndex + 1);
    }

    private static String normalizeAndRequirePath(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new BadRequestException("Invalid path");
        }
        return resourcePath.replace('\\', '/');
    }

    private static String ensureTypePath(String path, ResourceType type) {
        if (!type.equals(ResourceType.DIRECTORY)) {
            return path;
        }
        if (path.endsWith("/")) {
            return path;
        }
        return path + "/";
    }

    private static void validatePathTraversal(String path) {
        if (path.contains("..")) {
            throw new BadRequestException("Invalid path");
        }
    }

    private static void validateLeadingSlash(String path, ResourceType type) {
        if (type.equals(ResourceType.DIRECTORY)) {
            validateDirectoryPrefix(path);
            return;
        }
        validateAbsolutePath(path);
    }

    private static void validateDirectoryPrefix(String path) {
        if (path.startsWith("/") && !path.equals("/")) {
            throw new BadRequestException("Invalid path");
        }
    }

    private static void validateAbsolutePath(String path) {
        if (path.startsWith("/")) {
            throw new BadRequestException("Invalid path");
        }
    }

    public static String normalizeDirectoryPath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        String normalized = path.trim().replace('\\', '/');
        if ("/".equals(normalized)) {
            return normalized;
        }
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isBlank()) {
            return "/";
        }
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }
}
