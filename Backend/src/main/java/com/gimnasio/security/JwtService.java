package com.gimnasio.security;

import com.gimnasio.config.GimnasioProperties;
import com.gimnasio.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(GimnasioProperties propiedades) {
        this.clave = Keys.hmacShaKeyFor(propiedades.jwt().secret().getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = propiedades.jwt().expirationMs();
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claims(Map.of(
                        "uid", usuario.getId(),
                        "rol", usuario.getRol().getValor(),
                        "nombre", usuario.getNombreCompleto()))
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    public Claims parsear(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerEmail(String token) {
        return parsear(token).getSubject();
    }

    public boolean esValido(String token) {
        try {
            parsear(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
