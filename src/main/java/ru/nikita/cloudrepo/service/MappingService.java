package ru.nikita.cloudrepo.service;

import org.mapstruct.Mapper;
import ru.nikita.cloudrepo.dto.response.AuthResponseDto;
import ru.nikita.cloudrepo.repository.entity.User;


@Mapper(componentModel = "spring")
public interface MappingService {
    AuthResponseDto toResponseDto(User user);
}

