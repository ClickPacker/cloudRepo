package ru.nikita.cloudrepo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.nikita.cloudrepo.config.security.AuthUserDetails;
import ru.nikita.cloudrepo.dto.response.ResourceResponseDto;
import ru.nikita.cloudrepo.entity.DirectoryResource;
import ru.nikita.cloudrepo.service.impl.StorageService;
import ru.nikita.cloudrepo.service.ResourceUtils;
import ru.nikita.cloudrepo.utils.validate.IsPath;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("directory")
@Validated
public class DirectoryController {
    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<List<ResourceResponseDto>> getDirectoryHandler(
            @AuthenticationPrincipal AuthUserDetails details,
            @IsPath @RequestParam String path){
        DirectoryResource directory = ResourceUtils.parseDirectory(details.getUserBucket(), path);
        var body = storageService.getDirectoryContent(directory);
        return ResponseEntity
                .ok(body);
    }

    @PostMapping
    public ResponseEntity<ResourceResponseDto> createDirectoryHandler(
            @AuthenticationPrincipal AuthUserDetails details,
            @IsPath @RequestParam String path) {
        DirectoryResource directory = ResourceUtils.parseDirectory(details.getUserBucket(), path);
        storageService.createDirectory(directory);
        var body = storageService.getResource(directory);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(body);
    }
}
