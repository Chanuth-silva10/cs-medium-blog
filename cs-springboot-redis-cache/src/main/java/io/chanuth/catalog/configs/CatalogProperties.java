package io.chanuth.catalog.configs;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param simulatedDelay extra time every database read takes, to make the effect of caching visible
 * @param productsTtl    how long the full product list stays cached
 * @param searchTtl      how long one search result stays cached
 * @param categoriesTtl  how long the category list stays cached
 */
@ConfigurationProperties(prefix = "app.catalog")
public record CatalogProperties(
        @DefaultValue("2s") Duration simulatedDelay,
        @DefaultValue("10m") Duration productsTtl,
        @DefaultValue("2m") Duration searchTtl,
        @DefaultValue("1h") Duration categoriesTtl) {
}
