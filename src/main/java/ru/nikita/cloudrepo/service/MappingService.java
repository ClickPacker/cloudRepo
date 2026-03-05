package ru.nikita.cloudrepo.service;

import io.minio.messages.Bucket;
import org.mapstruct.Mapper;
import ru.nikita.cloudrepo.dto.response.BucketDto;
import ru.nikita.cloudrepo.dto.response.UserResponseDto;
import ru.nikita.cloudrepo.repository.entity.User;


@Mapper(componentModel = "spring")
public interface MappingService {
    UserResponseDto toResponseDto(User user);
}

