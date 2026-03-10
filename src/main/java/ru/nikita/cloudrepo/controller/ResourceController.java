package ru.nikita.cloudrepo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.nikita.cloudrepo.config.security.AuthUserDetails;
import ru.nikita.cloudrepo.dto.response.ResourceResponseDto;
import ru.nikita.cloudrepo.entity.DirectoryResource;
import ru.nikita.cloudrepo.entity.Resource;
import ru.nikita.cloudrepo.service.impl.StorageService;
import ru.nikita.cloudrepo.service.ResourceUtils;
import ru.nikita.cloudrepo.utils.ResourceData;
import ru.nikita.cloudrepo.utils.validate.IsPath;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("resource")
@Validated
public class ResourceController {
    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<ResourceResponseDto> getResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String path) {
        var body = storageService.getResource(ResourceUtils.parse(details.getUserBucket(), path));
        return ResponseEntity
                .ok(body);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResourceResponseDto> uploadResource(@AuthenticationPrincipal AuthUserDetails details, @IsPath @RequestParam String path, @RequestPart("file") MultipartFile file){
        DirectoryResource directory = ResourceUtils.parseDirectory(details.getUserBucket(), path);
        storageService.uploadResource(directory, file);

        String createdPath = directory.isRootDirectory()
                ? file.getOriginalFilename()
                : directory.getResourcePath() + file.getOriginalFilename();

        var body = storageService.getResource(ResourceUtils.parse(details.getUserBucket(), createdPath));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(body);
    }

    @GetMapping("search")
    public ResponseEntity<List<ResourceResponseDto>> searchResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String query){
        var results = storageService.searchResources(details.getUserBucket(), query);
        return ResponseEntity
                .ok(results);
    }

    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String path){
        storageService.deleteResource(ResourceUtils.parse(details.getUserBucket(), path));
        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("download")
    public ResponseEntity<byte[]> downloadResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String path){
        byte[] zipData = storageService.downloadResource(ResourceUtils.parse(details.getUserBucket(), path));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(new ResourceData(path).getName() + ".zip", StandardCharsets.UTF_8)
                .build());

        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .headers(headers)
                .contentLength(zipData.length)
                .body(zipData);
    }

    @GetMapping("move")
    public ResponseEntity<ResourceResponseDto> moveResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String from, @RequestParam String to){
        Resource source = ResourceUtils.parse(details.getUserBucket(), from);
        Resource destination = ResourceUtils.parse(details.getUserBucket(), to);
        var body = storageService.moveResource(source, destination);
        return ResponseEntity
                .ok(body);
    }

}
