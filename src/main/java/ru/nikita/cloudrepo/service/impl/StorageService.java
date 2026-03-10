package ru.nikita.cloudrepo.service.impl;

import org.springframework.web.multipart.MultipartFile;
import ru.nikita.cloudrepo.dto.response.ResourceResponseDto;
import ru.nikita.cloudrepo.entity.DirectoryResource;
import ru.nikita.cloudrepo.entity.Resource;

import java.util.List;

public interface StorageService {
    String getBucketIfExists(String bucketName);

    ResourceResponseDto getResource(Resource resource);

    void uploadResource(DirectoryResource resource, MultipartFile file);

    void deleteResource(Resource resource);

    byte[] downloadResource(Resource resource);

    ResourceResponseDto moveResource(Resource source, Resource destination);

    List<ResourceResponseDto> searchResources(String bucketName, String query);

    void createDirectory(DirectoryResource resource);

    List<ResourceResponseDto> getDirectoryContent(DirectoryResource resource);
}
