package ru.nikita.cloudrepo.dto.response;


import lombok.*;
import ru.nikita.cloudrepo.entity.ResourceType;

@EqualsAndHashCode(callSuper = true)
@Getter
@ToString(callSuper=true)
public class DirectoryResponseDto extends ObjectResponseDto{
    public DirectoryResponseDto(String path, String name) {
        super(path, name, ResourceType.DIRECTORY);
    }
}
