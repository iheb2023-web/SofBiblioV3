package com.sofrecom.backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    private String testSecretKey;
    private static final String TEST_USERNAME = "testuser@example.com";

    @BeforeEach
    void setUp() {
        // Générer dynamiquement une clé secrète pour les tests
        testSecretKey = generateTestSecretKey();
        // Injecter la clé secrète via reflection
        ReflectionTestUtils.setField(jwtService, "JWT_SECRET_KEY", testSecretKey);
        // Mock UserDetails
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);
    }

    /**
     * Génère une clé secrète aléatoirement pour les tests
     */
    private String generateTestSecretKey() {
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    @Test
    void generateToken_ShouldReturnValidToken() { //NOSONAR
        // When
        String token = jwtService.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length); // JWT has 3 parts separated by dots
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(TEST_USERNAME, extractedUsername);
    }



    @Test
    void extractClaim_ShouldExtractExpirationClaim() {
        // Given
        String token = jwtService.generateToken(userDetails);
        long expectedExpiration = System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 7); // 7 days

        // When
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        // Then
        assertNotNull(expiration);
        // Allow for small time differences during test execution (within 5 seconds)
        assertTrue(Math.abs(expiration.getTime() - expectedExpiration) < 5000);
    }

    @Test
    void isTokenValid_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithWrongUsername_ShouldReturnFalse() {
        // Given
        String token = jwtService.generateToken(userDetails);
        UserDetails differentUser = org.mockito.Mockito.mock(UserDetails.class);
        when(differentUser.getUsername()).thenReturn("different@example.com");

        // When
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Then
        assertFalse(isValid);
    }



    @Test
    void extractUsername_WithTamperedToken_ShouldThrowSignatureException() {
        // Given
        String token = jwtService.generateToken(userDetails);
        String tamperedToken = token.substring(0, token.length() - 5) + "tampered";

        // When & Then
        assertThrows(SignatureException.class, () -> {
            jwtService.extractUsername(tamperedToken);
        });
    }



    @Test
    void generateToken_ShouldCreateTokenWithCorrectExpiration() {
        // Given
        long beforeGeneration = System.currentTimeMillis();

        // When
        String token = jwtService.generateToken(userDetails);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        // Then
        long expectedExpirationTime = beforeGeneration + (1000 * 60 * 60 * 24 * 7); // 7 days
        long actualExpirationTime = expiration.getTime();
        // Allow for small time differences during test execution (within 5 seconds)
        assertTrue(Math.abs(actualExpirationTime - expectedExpirationTime) < 5000);
    }

    @Test
    void extractClaim_ShouldExtractSubjectClaim() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // Then
        assertEquals(TEST_USERNAME, subject);
    }



    @Test
    void isTokenValid_WithNullUserDetails_ShouldThrowException() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            jwtService.isTokenValid(token, null);
        });
    }

    /**
     * Helper method to create an expired token for testing
     */
    private String createExpiredToken() { //NOSONAR
        // Create a token that's already expired by setting the expiration to past time
        return io.jsonwebtoken.Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // 1 hour ago
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 30)) // 30 minutes ago
                .signWith(Keys.hmacShaKeyFor(
                                Base64.getDecoder().decode(testSecretKey)),
                        io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
    }
}