package com.project.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Generate JWT token
     */
    public String generateToken(Long userId, String username, String role) {
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("role", role)
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * Verify and decode JWT token
     */
    public DecodedJWT verifyToken(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);
    }

    /**
     * Parse token once, return all claims as a map-like object
     */
    public TokenPayload parseToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return new TokenPayload(
                jwt.getClaim("userId").asLong(),
                jwt.getClaim("username").asString(),
                jwt.getClaim("role").asString()
        );
    }

    /**
     * Get user ID from token
     */
    public Long getUserId(String token) {
        return verifyToken(token).getClaim("userId").asLong();
    }

    /**
     * Get username from token
     */
    public String getUsername(String token) {
        return verifyToken(token).getClaim("username").asString();
    }

    /**
     * Get role from token
     */
    public String getRole(String token) {
        return verifyToken(token).getClaim("role").asString();
    }

    /**
     * Payload extracted from JWT, avoids repeated verification
     */
    public record TokenPayload(Long userId, String username, String role) {
    }
}
