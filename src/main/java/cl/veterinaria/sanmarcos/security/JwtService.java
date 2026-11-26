package cl.veterinaria.sanmarcos.security;

import cl.veterinaria.sanmarcos.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    public static final String ACCESS_COOKIE = "vsm_access";
    public static final String REFRESH_COOKIE = "vsm_refresh";

    private final Key key;
    private final long expirationMs;
    private final boolean secureCookies;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes,
                      @Value("${app.cookies.secure:false}") boolean secureCookies) {
        if (secret == null || secret.isBlank() || secret.length() < 32)
            throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 caracteres.");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMinutes * 60_000;
        this.secureCookies = secureCookies;
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

    public void emitirCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(ACCESS_COOKIE, accessToken, expirationMs / 1000, "/").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(REFRESH_COOKIE, refreshToken, 7 * 24 * 60 * 60, "/api/v1/auth").toString());
    }

    public void limpiarCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(ACCESS_COOKIE, "", 0, "/").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(REFRESH_COOKIE, "", 0, "/api/v1/auth").toString());
    }

    public String leerRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (REFRESH_COOKIE.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

    private ResponseCookie cookie(String name, String value, long maxAgeSeconds, String path) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Lax")
                .path(path)
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }
}
