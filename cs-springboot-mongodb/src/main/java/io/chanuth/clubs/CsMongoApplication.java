package io.chanuth.clubs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class CsMongoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CsMongoApplication.class, args);
    }
}
