package ru.nikita.cloudrepo.utils.download;

import io.minio.MinioClient;
import io.minio.GetObjectArgs;
import io.minio.messages.Item;
import lombok.experimental.UtilityClass;
import ru.nikita.cloudrepo.entity.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@UtilityClass
public class DownloadUtils {
    public byte[] zipResourceToBytes(MinioClient minioClient, Resource resource, boolean isDirectory, List<Item> objects) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            if (isDirectory) {
                writeDirectoryEntries(zos, minioClient, resource, objects);
            } else {
                writeFileEntry(
                        zos,
                        minioClient,
                        resource.getBucket(),
                        resource.getResourcePath(),
                        resource.getResourceName()
                );
            }

            zos.finish();
            return baos.toByteArray();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void writeDirectoryEntries(ZipOutputStream zos, MinioClient minioClient, Resource resource, List<Item> objects) {
        String path = resource.getResourcePath();
        String basePrefix = "/".equals(path) ? "" : path;
        String rootFolder = "/".equals(path) ? "" : resource.getResourceName();
        Set<String> addedEntries = new HashSet<>();

        addRootDirectoryIfNeeded(zos, rootFolder, addedEntries);

        toZipEntries(path, basePrefix, rootFolder, objects)
                .forEach(entry -> writeZipEntry(zos, minioClient, resource.getBucket(), addedEntries, entry));
    }

    private List<ZipEntryData> toZipEntries(String path, String basePrefix, String rootFolder, List<Item> objects) {
        return objects.stream()
                .filter(item -> !item.objectName().equals(basePrefix))
                .map(item -> toZipEntryData(path, basePrefix, rootFolder, item))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<ZipEntryData> toZipEntryData(String path, String basePrefix, String rootFolder, Item item) {
        String relativePath = resolveRelativePath(path, basePrefix, item.objectName());
        if (relativePath.isBlank()) {
            return Optional.empty();
        }

        String entryName = toZipEntryName(rootFolder, relativePath);
        if (item.isDir() || item.objectName().endsWith("/")) {
            return Optional.of(ZipEntryData.directory(entryName));
        }
        return Optional.of(ZipEntryData.file(item.objectName(), entryName));
    }

    private String resolveRelativePath(String path, String basePrefix, String objectKey) {
        String relativePath = "/".equals(path)
                ? objectKey
                : objectKey.substring(basePrefix.length());
        if (relativePath.startsWith("/")) {
            return relativePath.substring(1);
        }
        return relativePath;
    }

    private void writeZipEntry(ZipOutputStream zos,
                               MinioClient minioClient,
                               String bucketName,
                               Set<String> addedEntries,
                               ZipEntryData entry) {
        if (entry.directory()) {
            writeDirectoryIfAbsent(zos, addedEntries, entry.entryName());
            return;
        }
        writeFileIfAbsent(zos, minioClient, bucketName, addedEntries, entry.objectKey(), entry.entryName());
    }

    private void writeDirectoryIfAbsent(ZipOutputStream zos, Set<String> addedEntries, String entryName) {
        String directoryEntry = entryName.endsWith("/") ? entryName : entryName + "/";
        if (!addedEntries.add(directoryEntry)) {
            return;
        }
        try {
            writeDirectoryEntry(zos, directoryEntry);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void writeFileIfAbsent(ZipOutputStream zos,
                                   MinioClient minioClient,
                                   String bucketName,
                                   Set<String> addedEntries,
                                   String objectKey,
                                   String entryName) {
        if (!addedEntries.add(entryName)) {
            return;
        }
        try {
            writeFileEntry(zos, minioClient, bucketName, objectKey, entryName);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void addRootDirectoryIfNeeded(ZipOutputStream zos, String rootFolder, Set<String> addedEntries) {
        if (rootFolder.isBlank()) {
            return;
        }
        writeDirectoryIfAbsent(zos, addedEntries, rootFolder);
    }

    private void writeFileEntry(ZipOutputStream zos,
                                MinioClient minioClient,
                                String bucketName,
                                String objectKey,
                                String entryName) throws Exception {
        zos.putNextEntry(new ZipEntry(entryName));
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .build())) {
            stream.transferTo(zos);
        } finally {
            zos.closeEntry();
        }
    }

    private void writeDirectoryEntry(ZipOutputStream zos, String entryName) throws IOException {
        String normalized = entryName.endsWith("/") ? entryName : entryName + "/";
        zos.putNextEntry(new ZipEntry(normalized));
        zos.closeEntry();
    }

    private String toZipEntryName(String rootFolder, String relativePath) {
        if (rootFolder == null || rootFolder.isBlank()) {
            return relativePath;
        }
        return rootFolder + "/" + relativePath;
    }

    private record ZipEntryData(String objectKey, String entryName, boolean directory) {
        private static ZipEntryData directory(String entryName) {
            return new ZipEntryData(null, entryName, true);
        }

        private static ZipEntryData file(String objectKey, String entryName) {
            return new ZipEntryData(objectKey, entryName, false);
        }
    }
}
