package com.example.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BackApplication {
    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("Admin@1234"));
        SpringApplication.run(BackApplication.class, args);
    }
}