package ru.nikita.cloudrepo.utils;

import lombok.AllArgsConstructor;
import ru.nikita.cloudrepo.entity.enums.ResourceType;

@AllArgsConstructor
public class ResourceData {
    private final String resource;

    public String getName() {
        String normalized = normalize(resource);
        if ("/".equals(normalized)) {
            return "root";
        }

        String value = normalized.endsWith("/") && normalized.length() > 1
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
        int delimiterIndex = value.lastIndexOf('/');
        return delimiterIndex < 0 ? value : value.substring(delimiterIndex + 1);
    }

    private String normalize(String value) {
        return value.replace('\\', '/');
    }
}
