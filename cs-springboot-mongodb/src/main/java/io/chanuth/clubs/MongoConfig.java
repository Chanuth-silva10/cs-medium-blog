package io.chanuth.clubs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
public class MongoConfig {

    /**
     * By default Spring Data converts a LocalDate to a Date at midnight in the time zone of the
     * server, so the stored value depends on where the application runs. The driver's own java.time
     * codecs store a LocalDate as midnight UTC, the same value on every machine.
     */
    @Bean
    MongoCustomConversions mongoCustomConversions() {
        return MongoCustomConversions.create(MongoCustomConversions.MongoConverterConfigurationAdapter::useNativeDriverJavaTimeCodecs);
    }
}
