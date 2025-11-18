package com.example.abra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@SpringBootApplication
@RestController
public class AbraApplication {

    public static void main(String[] args) {
        SpringApplication.run(AbraApplication.class, args);
    }

    @GetMapping("/")
    public String root(HttpServletRequest request) {
        String host = request.getServerName(); // Returns "shop.com"
        String scheme = request.getScheme();    // Returns "http" or "https"

        return "Welcome to Abra! Host: " + host + ", Scheme: " + scheme;

    }
}
