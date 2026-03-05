package ru.nikita.cloudrepo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.nikita.cloudrepo.config.security.AuthUserDetails;
import ru.nikita.cloudrepo.dto.response.ObjectResponseDto;
import ru.nikita.cloudrepo.service.StorageService;
import ru.nikita.cloudrepo.utils.validate.IsPath;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("directory")
@Validated
public class DirectoryController {
    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<List<ObjectResponseDto>> getDirectoryHandler(
            @AuthenticationPrincipal AuthUserDetails details,
            @IsPath @RequestParam String path){
        var body = storageService.getDirectoryContent(details.getId(), path);
        return ResponseEntity
                .ok(body);
    }

    @PostMapping
    public ResponseEntity<ObjectResponseDto> createDirectoryHandler(
            @AuthenticationPrincipal AuthUserDetails details,
            @IsPath @RequestParam String path) {
        storageService.createDirectory(details.getId(), path);
        var body = storageService.getResource(details.getId(), path);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(body);
    }
}
