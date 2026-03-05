package ru.nikita.cloudrepo.controller;

import org.simpleframework.xml.stream.Mode;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.nikita.cloudrepo.config.security.AuthUserDetails;

@Controller
public class HomeController {
    @GetMapping("home")
    private String homeHandler(@AuthenticationPrincipal AuthUserDetails details, Model model) {
        model.addAttribute("username", details.getUsername());
        return "home";
    }

    @GetMapping("settings")
    private String settingsHandler(@AuthenticationPrincipal AuthUserDetails details, Model model) {
        model.addAttribute("username", details.getUsername());
        return "settings";
    }

}
