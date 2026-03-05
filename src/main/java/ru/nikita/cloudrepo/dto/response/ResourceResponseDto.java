package ru.nikita.cloudrepo.dto.response;

import lombok.*;
import ru.nikita.cloudrepo.entity.ResourceType;

@EqualsAndHashCode(callSuper = true)
@Getter
@ToString(callSuper=true)
public class ResourceResponseDto extends ObjectResponseDto{
    private long size;

    public ResourceResponseDto(String path, String name, long size) {
        super(path, name, ResourceType.FILE);
        this.size = size;
    }
}
