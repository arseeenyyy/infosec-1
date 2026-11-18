package com.example.secureapi.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey key;
    private final long ttlMinutes;

    public JwtService(@Value("${app.jwt.secret}") String base64Secret,
                      @Value("${app.jwt.ttl-minutes}") long ttlMinutes) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.ttlMinutes = ttlMinutes;
    }

    public String generate(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlMinutes * 60_000);
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public long ttlSeconds() {
        return ttlMinutes * 60;
    }

    public Optional<String> validateAndGetSubject(String token) {
        try {
            return Optional.ofNullable(Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
