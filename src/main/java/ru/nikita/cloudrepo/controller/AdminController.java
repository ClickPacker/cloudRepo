package ru.nikita.cloudrepo.controller;


import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin")
public class AdminController {

    @DeleteMapping("delete")
    private void deleteUser() {
    }

    @PutMapping("update")
    private void updateUser() {

    }
}
