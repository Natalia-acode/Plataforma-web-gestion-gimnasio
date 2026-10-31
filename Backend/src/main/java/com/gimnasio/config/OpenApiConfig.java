package com.gimnasio.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiGimnasio() {
        final String esquema = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("API — Sistema de Gestion de Gimnasio")
                        .description("API REST del sistema de gestion de gimnasio. "
                                + "Autenticación JWT (Spring Security); convención /api/{recurso} con JSON.")
                        .version("0.1.0"))
                .addSecurityItem(new SecurityRequirement().addList(esquema))
                .components(new Components().addSecuritySchemes(esquema,
                        new SecurityScheme()
                                .name(esquema)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
