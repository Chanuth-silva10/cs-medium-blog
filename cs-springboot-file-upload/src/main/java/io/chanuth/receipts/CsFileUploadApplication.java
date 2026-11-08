package io.chanuth.receipts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CsFileUploadApplication {

    public static void main(String[] args) {
        SpringApplication.run(CsFileUploadApplication.class, args);
    }
}
