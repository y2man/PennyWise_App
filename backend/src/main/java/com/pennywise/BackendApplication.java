package com.pennywise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.File;

@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        // Create DB directory BEFORE Spring Boot starts
        // (Spring tries to open the SQLite file during startup)
        File dbDir = new File(System.getProperty("user.home"), ".pennywise");
        if (!dbDir.exists()) {
            boolean ok = dbDir.mkdirs();
            System.out.println("[PennyWise] DB directory " + (ok ? "created" : "failed") + ": " + dbDir.getAbsolutePath());
        } else {
            System.out.println("[PennyWise] DB directory exists: " + dbDir.getAbsolutePath());
        }
        SpringApplication.run(BackendApplication.class, args);
    }
}
