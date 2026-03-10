package ru.nikita.cloudrepo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.nikita.cloudrepo.entity.enums.ResourceType;

@Getter
@AllArgsConstructor
public class ResourceResponseDto {
    protected String path;
    protected String name;
    protected ResourceType type;
}
