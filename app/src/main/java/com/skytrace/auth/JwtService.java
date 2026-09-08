package com.skytrace.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @jakarta.annotation.PostConstruct
    void validateConfiguration() {
        getSigningKey();
    }

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET doit etre defini");
        }
        try {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        } catch (WeakKeyException exception) {
            throw new IllegalStateException("JWT_SECRET doit contenir au moins 32 octets pour HS256", exception);
        }
    }

    public String genererToken(UserDetails userDetails, String role) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(Map.of("role", role))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extraireLogin(String token) {
        return extraireClaim(token, Claims::getSubject);
    }

    public String extraireRole(String token) {
        Claims claims = extraireTousLesClaims(token);
        return claims.get("role", String.class);
    }

    public boolean estValide(String token, UserDetails userDetails) {
        try {
            String login = extraireLogin(token);
            return login.equals(userDetails.getUsername()) && !estExpire(token);
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean estExpire(String token) {
        return extraireClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extraireClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extraireTousLesClaims(token);
        return resolver.apply(claims);
    }

    private Claims extraireTousLesClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
