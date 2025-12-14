package com.example.abra;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequiredArgsConstructor
@EnableScheduling
public class AbraApplication {
    public static void main(String[] args) {
        SpringApplication.run(AbraApplication.class, args);
    }
}
