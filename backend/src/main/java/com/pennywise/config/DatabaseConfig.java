package com.pennywise.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.File;

/**
 * Ensures the .pennywise directory exists before JPA tries to create the DB.
 */
@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @PostConstruct
    public void ensureDbDirectory() {
        File dir = new File(System.getProperty("user.home"), ".pennywise");
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) log.info("Created database directory: {}", dir.getAbsolutePath());
            else         log.warn("Could not create directory: {}", dir.getAbsolutePath());
        }
        log.info("PennyWise DB location: {}", new File(dir, "pennywise.db").getAbsolutePath());
    }
}
