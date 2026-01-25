package com.example.abra.security;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET =
        "VGhpcyIsImlzIiwiYSIsInZlcnkiLCJsb25nIiwic2VjcmV0Iiwia2V5IiwidGhhdCIsIndvcmRzIiwiZm9yIiwiSFMyNTYiXQ==";
    private static final long TEST_EXPIRATION_MS = 1000 * 60; // 1 minuta

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(
            jwtService,
            "jwtExpirationMs",
            TEST_EXPIRATION_MS
        );
    }

    @Test
    void generateToken_createsValidToken() {
        String username = "Mateusz";

        String token = jwtService.generateToken(username);

        assertNotNull(token);
        assertEquals(username, jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, username));
    }

    @Test
    void isTokenValid_failsForWrongUsername() {
        String token = jwtService.generateToken("Mateusz");

        boolean isValid = jwtService.isTokenValid(token, "Hacker");

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_throwsException_whenTokenExpired() {
        Date pastDate = new Date(System.currentTimeMillis() - 1000);
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));

        String expiredToken = Jwts.builder()
            .setSubject("Mateusz")
            .setExpiration(pastDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.isTokenValid(expiredToken, "Mateusz");
        });
    }
}
