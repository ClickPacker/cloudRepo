package ru.nikita.cloudrepo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.nikita.cloudrepo.service.ResourceService;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("resource")
public class ResourceController {
    private final ResourceService resourceService;

    @GetMapping
    private ResponseEntity<Map<String, String>> resourcePathHandler(@RequestParam String path) {
        return new ResponseEntity<>(
                new HashMap<>(),
                HttpStatusCode.valueOf(200)
        );
    }

    @DeleteMapping
    private ResponseEntity<HttpStatus> deleteResourceHandler(@RequestParam String path) {
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("download")
    private ResponseEntity<HttpStatus> downloadHandler(@RequestParam String path) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @GetMapping("move")
    private ResponseEntity<HttpStatus> moveHandler(@RequestParam String from, @RequestParam String to) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @GetMapping("query")
    private ResponseEntity<HttpStatus> moveHandler(@RequestParam URLEncoder query) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @PostMapping
    private ResponseEntity<HttpStatus> uploadHandler(@RequestParam String path, @RequestBody MultipartFile fileInput) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
