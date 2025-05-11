package com.example.LibraryManagement.Config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
public class JwtUtil {
    private final String SECRET_KEY;

    public JwtUtil(String secretKey) {
        this.SECRET_KEY = secretKey;
    }
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(userDetails.getUsername())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour validity
                .and()
                .signWith(getSignKey())
                .compact();
    }
    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.getSubject());
    }
    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
    private <T> T extractClaim(String token, Function<io.jsonwebtoken.Claims, T> claimsResolver) {
        return claimsResolver.apply(Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload());
    }
    private boolean isTokenExpired(String token) {
        return extractClaim(token, claims -> claims.getExpiration().before(new Date()));
    }
}
