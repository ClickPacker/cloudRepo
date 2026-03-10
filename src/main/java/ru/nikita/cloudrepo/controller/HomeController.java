package ru.nikita.cloudrepo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.nikita.cloudrepo.config.security.AuthUserDetails;

import static ru.nikita.cloudrepo.service.ResourceUtils.normalizeDirectoryPath;

@Controller
public class HomeController {
    @GetMapping
    private String homeHandler(
            @AuthenticationPrincipal AuthUserDetails details,
            @RequestParam(required = false) String path,
            Model model
    ) {
        model.addAttribute("username", details.getUsername());
        model.addAttribute("activePage", "home");
        model.addAttribute("currentPath", normalizeDirectoryPath(path));
        return "home";
    }

    @GetMapping("settings")
    private String settingsHandler(@AuthenticationPrincipal AuthUserDetails details, Model model) {
        model.addAttribute("username", details.getUsername());
        model.addAttribute("activePage", "settings");
        return "settings";
    }
}
