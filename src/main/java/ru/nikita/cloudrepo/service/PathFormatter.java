package ru.nikita.cloudrepo.service;

import ru.nikita.cloudrepo.exception.BadRequestException;

public class PathFormatter {
    static String sanitizeUploadedPath(String originalFilename) {
        String normalized = originalFilename.replace('\\', '/');
        if (normalized.startsWith("/") || normalized.contains("..") || normalized.isBlank()) {
            throw new BadRequestException("Invalid request body");
        }
        return normalized;
    }

    static String fileName(String path) {
        int delimiterIndex = path.lastIndexOf('/');
        return delimiterIndex < 0 ? path : path.substring(delimiterIndex + 1);
    }

    static String directoryName(String path) {
        if ("/".equals(path)) {
            return "root";
        }
        String clean = path.substring(0, path.length() - 1);
        int delimiterIndex = clean.lastIndexOf('/');
        return delimiterIndex < 0 ? clean : clean.substring(delimiterIndex + 1);
    }

    static String parentPath(String path) {
        String clean = path.substring(0, path.length() - 1);
        int delimiterIndex = clean.lastIndexOf('/');
        if (delimiterIndex < 0) {
            return "";
        }
        return clean.substring(0, delimiterIndex + 1);
    }




}
