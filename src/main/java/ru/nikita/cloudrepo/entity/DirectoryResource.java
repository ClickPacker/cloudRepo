package ru.nikita.cloudrepo.entity;

import ru.nikita.cloudrepo.entity.enums.ResourceType;

public class DirectoryResource extends Resource {
    public DirectoryResource(String resourcePath, String bucket) {
        super(resourcePath, ResourceType.DIRECTORY, bucket);
    }

    @Override
    public String getParentPath() {
        String path = trimTrailingSlash(getResourcePath());
        int delimiterIndex = path.lastIndexOf('/');
        if (delimiterIndex < 0) {
            return "";
        }
        return path.substring(0, delimiterIndex);
    }

    @Override
    public String getResourceName() {
        String path = trimTrailingSlash(getResourcePath());
        if (path.isEmpty()) {
            return "root";
        }
        int delimiterIndex = path.lastIndexOf('/');
        if (delimiterIndex < 0) {
            return path;
        }
        return path.substring(delimiterIndex + 1);
    }

    private String trimTrailingSlash(String value) {
        if ("/".equals(value)) {
            return "";
        }
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }


    public boolean isRootDirectory() {
        return "/".equals(this.getResourcePath());
    }
}
