package cl.veterinaria.sanmarcos.security;

import cl.veterinaria.sanmarcos.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final Key key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        if (secret == null || secret.isBlank() || secret.length() < 32)
            throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 caracteres.");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMinutes * 60_000;
    }

    public String crear(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder().subject(usuario.getCorreo()).issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expirationMs)).signWith(key).compact();
    }

    public String correo(String token) {
        return Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }
}
