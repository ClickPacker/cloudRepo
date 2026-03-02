package ru.nikita.cloudrepo.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nikita.cloudrepo.service.DirectoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("directory")
public class DirectoryController {
    private final DirectoryService directoryService;

    @GetMapping
    private ResponseEntity getDirectoryHandler() {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PostMapping
    private ResponseEntity createDirectoryHandler() {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }


}
