package com.example.formation_devsecops.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BeeStore API")
                        .description("API REST de gestion des produits"
                                + " - Formation DevSecOps BeeDigital")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BeeDigital Training")
                                .email("contact@beedigital.tn")
                                .url("https://www.beedigital.tn"))
                        .license(new License()
                                .name("MIT")));
    }

}

