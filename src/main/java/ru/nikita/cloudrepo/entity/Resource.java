package ru.nikita.cloudrepo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.nikita.cloudrepo.entity.enums.ResourceType;


@Getter
@AllArgsConstructor
public abstract class Resource {
    private String resourcePath;
    private ResourceType resourceType;
    private String bucket;

    public abstract String getParentPath();

    public abstract String getResourceName();

    public boolean compareTypes(Resource resource) {
        return this.getResourceType() == resource.getResourceType();
    }

    public boolean isDir() {
        return this.getResourceType().equals(ResourceType.DIRECTORY);
    }

    public boolean isFile() {
        return this.getResourceType().equals(ResourceType.FILE);
    }
}
