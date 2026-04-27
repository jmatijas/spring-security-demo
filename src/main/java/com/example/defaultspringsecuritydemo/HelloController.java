package com.example.defaultspringsecuritydemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/")
    String hello() {
        log.info("hello()");
        return "Hello World!";
    }

    @GetMapping("/admin")
    String helloAdmin() {
        log.info("helloAdmin()");
        return "Hello Admin!";
    }

    @GetMapping("/user")
    String helloUser() {
        log.info("helloUser()");
        return "Hello User!";
    }

    @GetMapping("/files/{*filePath}")
    String getFiles(@PathVariable String filePath) {
        log.info("getFiles() - filePath: [{}]", filePath);
        return "filePath: " + filePath;
    }
}
