package com.learn4you.account_service.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;


public final class EnvLoader {
    private static final Logger logger = LoggerFactory.getLogger(EnvLoader.class);

    private static String resolveEnvFile(String customEnvFile) {
        // Try custom file
        if (customEnvFile != null && Files.exists(Paths.get(customEnvFile))) {
            return customEnvFile;
        }

        if (customEnvFile == null) {
            logger.info("Custom env file is null --> try to load default values in .env file");
        }

        // Fallback to .env
        String defaultEnvFile = ".env";
        if (Files.exists(Paths.get(defaultEnvFile))) {
            logger.info("Loading environment variables from default values in .env file");
            return defaultEnvFile;
        }

        // Nothing found - fail fast
        throw new IllegalStateException("No environment file found (tried: custom and .env)");
    }

    public static void loadEnv() {
        loadEnv(null);
    }

    public static void loadEnv(String customEnvFile) {
        String envFile = resolveEnvFile(customEnvFile);
        logger.info("Loading env file: {}", envFile);
        Dotenv dotenv = Dotenv.configure()
                              .filename(envFile)
                              .ignoreIfMalformed()
                              .ignoreIfMissing()
                              .load();

        // Apply all variables to system properties
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }
}