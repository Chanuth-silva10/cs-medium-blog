package io.chanuth.receipts.web;

import io.chanuth.receipts.storage.ReceiptStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The quick way to make uploaded files reachable by URL: map a URL path straight onto the folder.
 * It is OFF by default, because it makes EVERY file in the folder public, see the article.
 * Turn it on with app.storage.serve-statically=true, for files that really are meant to be public.
 */
@Configuration
@ConditionalOnProperty(name = "app.storage.serve-statically", havingValue = "true")
public class StaticUploadsConfig implements WebMvcConfigurer {

    private final ReceiptStorage storage;

    public StaticUploadsConfig(ReceiptStorage storage) {
        this.storage = storage;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Path.toUri() writes the correct "file:" URL for the current system: file:///D:/... on
        // Windows, file:///home/... on Linux. Nothing to hand-craft per operating system.
        registry.addResourceHandler("/public-uploads/**")
                .addResourceLocations(storage.root().toUri().toString());
    }
}
