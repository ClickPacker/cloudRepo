package ru.nikita.cloudrepo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.nikita.cloudrepo.entity.ResourceType;

@Getter
@AllArgsConstructor
public class ObjectResponseDto {
    protected String path;
    protected String name;
    protected ResourceType type;
}
