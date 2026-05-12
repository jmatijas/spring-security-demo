package com.example.defaultspringsecuritydemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/")
    String helloHome(Model model, Authentication authentication) {
        log.info("helloHome()");
        if (authentication != null) {
            log.info("User is: {}", authentication.getName());
            model.addAttribute("user", authentication.getPrincipal());
        } else {
            System.out.println("Authentication is NULL in Controller!");
        }
        return "home";
    }

    @GetMapping("/login")
    String login() {
        log.info("login()");
        return "login";
    }

    @GetMapping("/public")
    @ResponseBody
    String helloPublic() {
        log.info("helloPublic()");
        return "Hello Public!";
    }

    @GetMapping("/admin")
    @ResponseBody
    String helloAdmin() {
        log.info("helloAdmin()");
        return "Hello Admin!";
    }

    @GetMapping("/user")
    @ResponseBody
    String helloUser() {
        log.info("helloUser()");
        return "Hello User!";
    }

    @GetMapping("/files/{*filePath}")
    @ResponseBody
    String getFiles(@PathVariable String filePath) {
        log.info("getFiles() - filePath: [{}]", filePath);
        return "filePath: " + filePath;
    }
}
