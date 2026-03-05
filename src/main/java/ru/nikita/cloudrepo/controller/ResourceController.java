package ru.nikita.cloudrepo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.nikita.cloudrepo.config.security.AuthUserDetails;
import ru.nikita.cloudrepo.dto.response.ObjectResponseDto;
import ru.nikita.cloudrepo.service.StorageService;
import ru.nikita.cloudrepo.utils.ResourceData;
import ru.nikita.cloudrepo.utils.validate.IsPath;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("resource")
@Validated
public class ResourceController {
    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<ObjectResponseDto> getResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String path){
        var body = storageService.getResource(details.getId(), path);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ObjectResponseDto> uploadResource(@AuthenticationPrincipal AuthUserDetails details, @IsPath @RequestParam String path, @RequestPart("file") MultipartFile file){
        storageService.upload(details.getId(), path, file);
        var body = storageService.getResource(details.getId(), path + file.getOriginalFilename());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @GetMapping("search")
    public ResponseEntity<List<ObjectResponseDto>> searchResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String query){
        var results = storageService.search(details.getId(), query);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(results);
    }

    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String path){
        storageService.deleteResource(details.getId(), path);
        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("download")
    public ResponseEntity<byte[]> downloadResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String path){
        byte[] zipData = storageService.downloadResource(details.getId(), path);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(new ResourceData(path).getName() + ".zip")
                .build());

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(zipData);

    }

    @GetMapping("move")
    public ResponseEntity<ObjectResponseDto> moveResource(@AuthenticationPrincipal AuthUserDetails details, @RequestParam String from, @RequestParam String to){
        storageService.moveResource(details.getId(), from, to);
        var body = storageService.getResource(details.getId(), to);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

}
