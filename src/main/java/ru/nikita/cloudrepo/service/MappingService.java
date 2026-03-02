package ru.nikita.cloudrepo.service;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Service;
import ru.nikita.cloudrepo.dto.response.UserResponseDto;
import ru.nikita.cloudrepo.repository.entity.User;


@Mapper(componentModel = "spring")
public interface MappingService {
    UserResponseDto toResponseDto(User user);
}

