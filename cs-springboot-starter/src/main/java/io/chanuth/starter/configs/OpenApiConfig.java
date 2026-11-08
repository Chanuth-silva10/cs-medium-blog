package io.chanuth.starter.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    // Read the port from the same property the server uses, so the docs never point at the wrong place.
    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI devCraftOpenApi() {
        Contact contact = new Contact()
                .name("CS Solution")
                .url("https://github.com/Chanuth-silva10/cs-medium-blog");
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Local environment");
        License license = new License().name("MIT").url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("DevCraft Project API")
                .version("1.0.0")
                .description("Manage DevCraft projects over a REST API.")
                .contact(contact)
                .license(license);

        return new OpenAPI().info(info).servers(List.of(localServer));
    }
}
