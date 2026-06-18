package com.pennywise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        loadLocalEnvironment();

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

    private static void loadLocalEnvironment() {
        List<Path> envFiles = List.of(
                Path.of(".env"),
                Path.of("backend", ".env")
        );

        for (Path envFile : envFiles) {
            if (Files.exists(envFile)) {
                try {
                    for (String line : Files.readAllLines(envFile)) {
                        applyEnvLine(line);
                    }
                    System.out.println("[PennyWise] Loaded environment from " + envFile.toAbsolutePath());
                    return;
                } catch (IOException e) {
                    System.out.println("[PennyWise] Failed to read env file " + envFile.toAbsolutePath() + ": " + e.getMessage());
                }
            }
        }

        System.out.println("[PennyWise] No local .env file found; JWT_SECRET may be missing.");
    }

    private static void applyEnvLine(String line) {
        if (line == null) {
            return;
        }

        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return;
        }

        int separator = trimmed.indexOf('=');
        if (separator <= 0) {
            return;
        }

        String key = trimmed.substring(0, separator).trim();
        String value = trimmed.substring(separator + 1).trim();
        if (key.isEmpty() || value.isEmpty()) {
            return;
        }

        if (System.getProperty(key) == null && System.getenv(key) == null) {
            System.setProperty(key, value);
        }
    }
}
