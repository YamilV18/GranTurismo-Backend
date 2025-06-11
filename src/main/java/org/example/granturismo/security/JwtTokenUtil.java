package org.example.granturismo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger; // Importar Logger
import org.slf4j.LoggerFactory; // Importar LoggerFactory
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class); // Inicializar Logger

    private final long JWT_TOKEN_VALIDITY = 5 * 60 * 60 * 1000; // 5 horas

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")));
        claims.put("test", "syscenterlife-value-test");

        // Añadir el ID del usuario a los claims si es una instancia de CustomUserDetails
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            claims.put("userId", customUserDetails.getIdUsuario()); // <-- Aquí se añade el ID
        } else {
            logger.warn("UserDetails no es una instancia de CustomUserDetails para el usuario '{}'. El claim 'userId' no se añadirá al token.", userDetails.getUsername());
        }

        return doGenerateToken(claims, userDetails.getUsername());
    }

    private String doGenerateToken(Map<String, Object> claims, String username) {
        SecretKey key = Keys.hmacShaKeyFor(this.secret.getBytes());

        return Jwts.builder()
                .claims(claims)
                .subject(username) // El subject sigue siendo el username (email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(key)
                .compact();
    }

    // utils
    public Claims getAllClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(this.secret.getBytes());
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver){
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // Nuevo método para obtener el ID del usuario desde el token
    public Long getUserIdFromToken(String token) {
        // Asume que el claim 'userId' es de tipo Long
        return getClaimFromToken(token, claims -> claims.get("userId", Long.class));
    }

    public Date getExpirationDateFromToken(String token){
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token){
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails){
        final String username = getUsernameFromToken(token);
        return (username.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token));
    }
}