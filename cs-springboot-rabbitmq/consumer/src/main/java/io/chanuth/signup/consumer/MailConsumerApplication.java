package io.chanuth.signup.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MailProperties.class)
public class MailConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailConsumerApplication.class, args);
    }
}
