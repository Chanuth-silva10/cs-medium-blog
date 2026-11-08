package io.chanuth.catalog.configs;

import io.chanuth.catalog.dtos.CategoryListResponse;
import io.chanuth.catalog.dtos.ProductListResponse;
import java.time.Duration;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

@Configuration
@EnableCaching
public class CacheConfiguration implements CachingConfigurer {

    /**
     * Spring Boot creates the Redis cache manager; we only adjust it. This is used when
     * spring.cache.type is redis, and ignored otherwise (the tests use a simple in-memory cache).
     */
    @Bean
    RedisCacheManagerBuilderCustomizer redisCacheCustomizer(RedisConnectionFactory connectionFactory,
                                                           CatalogProperties properties) {
        return builder -> builder
                // A LOCKING writer is what makes @Cacheable(sync = true) work with Redis: it takes a short
                // lock in Redis while the first request loads the value, so the others wait instead of
                // querying the database too. The lock expires after 30 seconds, so a crashed instance cannot
                // leave it behind. Clearing a whole cache uses SCAN, which walks the keys in small steps;
                // the default KEYS command blocks Redis for everyone while it scans the entire database.
                .cacheWriter(RedisCacheWriter.lockingRedisCacheWriter(connectionFactory, Duration.ofMillis(50),
                        RedisCacheWriter.TtlFunction.just(Duration.ofSeconds(30)), BatchStrategies.scan(1000)))
                // Evictions inside a transaction wait until it commits. Otherwise a read in the gap could
                // put the old data straight back into the cache.
                .transactionAware()
                .withCacheConfiguration(CacheNames.ALL_PRODUCTS,
                        jsonCache(ProductListResponse.class, properties.productsTtl()))
                .withCacheConfiguration(CacheNames.PRODUCT_SEARCH,
                        jsonCache(ProductListResponse.class, properties.searchTtl()))
                .withCacheConfiguration(CacheNames.CATEGORIES,
                        jsonCache(CategoryListResponse.class, properties.categoriesTtl()));
    }

    /**
     * Each cache stores one known type as plain JSON, and every entry expires on its own after the TTL.
     * A typed serializer writes no Java class names into Redis, so nothing in the cache can tell the
     * application to build an arbitrary class when it reads the data back.
     */
    private static <T> RedisCacheConfiguration jsonCache(Class<T> type, Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .prefixCacheNameWith("devcraft:catalog:")
                .serializeValuesWith(SerializationPair.fromSerializer(new JacksonJsonRedisSerializer<>(type)));
    }

    /**
     * If Redis is down, a cache read or write fails. Without this, that failure becomes a 500 for the
     * user. With it, the problem is logged and the request simply goes to the database, slower but working.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler(false);
    }
}
