package ru.nikita.cloudrepo.dto.response;

import lombok.*;
import ru.nikita.cloudrepo.entity.enums.ResourceType;
import ru.nikita.cloudrepo.service.ResourceUtils;

@EqualsAndHashCode(callSuper = true)
@Getter
@ToString(callSuper=true)
public class FileResponseDto extends ResourceResponseDto {
    private long size;

    public FileResponseDto(String objectName, long size) {
        super(
                ResourceUtils.parentPath(objectName),
                ResourceUtils.fileName(objectName),
                ResourceType.FILE
        );
        this.size = size;
    }
}
