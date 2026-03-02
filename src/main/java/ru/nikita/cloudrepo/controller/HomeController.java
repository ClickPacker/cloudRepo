package ru.nikita.cloudrepo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("home")
    private String homeHandler() {
        return "index";
    }
}
