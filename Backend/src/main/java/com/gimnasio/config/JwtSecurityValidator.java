package com.gimnasio.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;

/**
 * Valida la configuración de seguridad al arrancar la aplicacion.
 * En perfil prod rechaza el uso de secretos por defecto de JWT.
 */
@Configuration
@EnableConfigurationProperties(GimnasioProperties.class)
@Profile("prod")
public class JwtSecurityValidator {

    private final GimnasioProperties propiedades;

    public JwtSecurityValidator(GimnasioProperties propiedades) {
        this.propiedades = propiedades;
    }

    @PostConstruct
    public void validar() {
        String secreto = propiedades.jwt().secret();
        if (secreto == null || secreto.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET es obligatorio en perfil prod. Defina la variable de entorno JWT_SECRET.");
        }
        if (secreto.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 32 caracteres en perfil prod.");
        }
        if (secreto.startsWith("dev-") || secreto.startsWith("gimnasio-clave-jwt-desarrollo")) {
            throw new IllegalStateException(
                    "No se permite usar el secreto JWT por defecto de desarrollo en perfil prod.");
        }
    }
}
