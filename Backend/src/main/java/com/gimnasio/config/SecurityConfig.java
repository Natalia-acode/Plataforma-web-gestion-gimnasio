package com.gimnasio.config;

import com.gimnasio.security.JwtAuthFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(GimnasioProperties.class)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final GimnasioProperties propiedades;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, GimnasioProperties propiedades) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.propiedades = propiedades;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorizacion -> autorizacion
                        // Público: autenticación, catálogo de planes y documentación
                        .requestMatchers("/api/auth/login", "/api/auth/registro",
                                "/api/auth/recuperar-contrasena").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/planes/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/aforo/ocupacion").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/clases").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Recepción
                        .requestMatchers("/api/checkin/**").hasAnyRole("RECEPCIONISTA", "ADMINISTRADOR")
                        .requestMatchers("/api/cola/gestion/**").hasAnyRole("RECEPCIONISTA", "ADMINISTRADOR")
                        // Entrenador
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/clases")
                        .hasAnyRole("ENTRENADOR", "ADMINISTRADOR")
                        .requestMatchers("/api/clases/*/asistencia").hasAnyRole("ENTRENADOR", "ADMINISTRADOR")
                        .requestMatchers("/api/clases/*/asistentes").hasAnyRole("ENTRENADOR", "ADMINISTRADOR")
                        // Centro de notificaciones: cualquier usuario autenticado
                        .requestMatchers("/api/usuarios/notificaciones/**").authenticated()
                        // Administración
                        .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/reportes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/planes").hasRole("ADMINISTRADOR")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/planes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/zonas/**").hasRole("ADMINISTRADOR")
                        // Resto: cualquier usuario autenticado
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(List.of(propiedades.cors().allowedOrigins().split(",")));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("*"));
        configuracion.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }
}
