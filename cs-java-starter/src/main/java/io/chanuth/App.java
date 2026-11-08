package io.chanuth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the CS Java Starter. Shows structured logging with SLF4J/Logback
 * and JSON serialization with Jackson.
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting cs-java-starter");

        AppInfo info = new AppInfo("cs-java-starter", "1.0.0");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(info);

        logger.info("App info as JSON: {}", json);
    }

    public record AppInfo(String name, String version) {
    }
}
