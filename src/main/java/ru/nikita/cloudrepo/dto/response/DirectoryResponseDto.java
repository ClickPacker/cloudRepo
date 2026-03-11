package ru.nikita.cloudrepo.dto.response;


import lombok.*;
import ru.nikita.cloudrepo.entity.enums.ResourceType;
import ru.nikita.cloudrepo.service.ResourceUtils;

@EqualsAndHashCode(callSuper = true)
@Getter
@ToString(callSuper=true)
public class DirectoryResponseDto extends ResourceResponseDto {
    public DirectoryResponseDto(String objectName) {
        super(
                ResourceUtils.parentPath(objectName),
                ResourceUtils.directoryName(objectName),
                ResourceType.DIRECTORY
        );
    }
}
