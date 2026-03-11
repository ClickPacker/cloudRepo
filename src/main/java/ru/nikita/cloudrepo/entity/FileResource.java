package ru.nikita.cloudrepo.entity;


import ru.nikita.cloudrepo.entity.enums.ResourceType;

public class FileResource extends Resource {
    public FileResource(String resourcePath, String bucket) {
        super(resourcePath, ResourceType.FILE, bucket);
    }

    @Override
    public String getParentPath() {
        String path = getResourcePath();
        int delimiterIndex = path.lastIndexOf('/');
        if (delimiterIndex < 0) {
            return "";
        }
        return path.substring(0, delimiterIndex);
    }

    @Override
    public String getResourceName() {
        String path = getResourcePath();
        int delimiterIndex = path.lastIndexOf('/');
        if (delimiterIndex < 0) {
            return path;
        }
        return path.substring(delimiterIndex + 1);
    }
}
