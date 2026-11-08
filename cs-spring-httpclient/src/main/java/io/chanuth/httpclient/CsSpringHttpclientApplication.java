package io.chanuth.httpclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CsSpringHttpclientApplication {

    public static void main(String[] args) {
        SpringApplication.run(CsSpringHttpclientApplication.class, args);
    }
}
