package ru.nikita.cloudrepo.controller;

import io.minio.GetObjectResponse;
import io.minio.StatObjectResponse;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.nikita.cloudrepo.config.security.AuthUserDetails;
import ru.nikita.cloudrepo.dto.response.BucketDto;
import ru.nikita.cloudrepo.service.CloudRepositoryService;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("resource")
public class ResourceController {
    private final CloudRepositoryService cloudRepositoryService;

    @GetMapping
    private void getResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String path) {
        cloudRepositoryService.getResourceInfo(details.getUsername(), path);
    }

    @PostMapping
    private void createResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String path, MultipartFile[] files) {
        cloudRepositoryService.upload(details.getUsername(), path, files);
    }

    @GetMapping("search")
    private void searchResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String query) {}

    @DeleteMapping
    private void deleteResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String path) {
        cloudRepositoryService.deleteResource(details.getUsername(), path);
    }

    @GetMapping("download")
    private void downloadResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String path) {}

    @GetMapping("move")
    private void moveResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String from, @RequestParam String to) {}

}
