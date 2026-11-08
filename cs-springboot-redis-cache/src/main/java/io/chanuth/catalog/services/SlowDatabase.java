package io.chanuth.catalog.services;

import io.chanuth.catalog.configs.CatalogProperties;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Our seed data is tiny, so real queries are instant. This adds a configurable pause to every catalogue
 * read, standing in for a big, busy database, and counts the reads so we can see what the cache saves.
 */
@Component
public class SlowDatabase {

    private static final Logger log = LoggerFactory.getLogger(SlowDatabase.class);

    private final CatalogProperties properties;
    private final AtomicInteger reads = new AtomicInteger();

    public SlowDatabase(CatalogProperties properties) {
        this.properties = properties;
    }

    public void read(String what) {
        log.info("Database read #{}: {}", reads.incrementAndGet(), what);
        try {
            Thread.sleep(properties.simulatedDelay().toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public int reads() {
        return reads.get();
    }
}
