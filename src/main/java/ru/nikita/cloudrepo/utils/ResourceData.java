package ru.nikita.cloudrepo.utils;

import lombok.AllArgsConstructor;
import ru.nikita.cloudrepo.entity.ResourceType;

import java.io.File;

// TODO: переписать логику под парсинг строк, а то долго

@AllArgsConstructor
public class ResourceData {
    private String resource;

    public String getName() {
        return new File(resource).getName();
    }

    public String getPathToFile() {
        return new File(resource).getParent().replace("\\", "/") + "/";
    }

    public ResourceType getType() {
        return resource.endsWith("/") ? ResourceType.DIRECTORY : ResourceType.FILE;
    }
}
