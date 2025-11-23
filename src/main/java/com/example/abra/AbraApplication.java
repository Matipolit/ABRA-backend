package com.example.abra;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequiredArgsConstructor
public class AbraApplication {

    // You can keep the main method and injections if needed elsewhere,
    // but DELETE the root() method entirely.

    public static void main(String[] args) {
        SpringApplication.run(AbraApplication.class, args);
    }
}
