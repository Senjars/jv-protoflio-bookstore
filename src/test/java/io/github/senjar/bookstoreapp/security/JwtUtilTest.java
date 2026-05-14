package io.github.senjar.bookstoreapp.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("JWT Util Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        String secretKey = "ThisIsAVeryLongSecretKeyForTestingJwtTokenGenerationAndValidation";
        long expiration = 3600000; // 1 hour
        jwtUtil = new JwtUtil(secretKey);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
    }

    @Test
    @DisplayName("Generate token successfully")
    void generateToken_ValidUsername_ReturnsToken() {
        // Given
        String username = "test@example.com";

        // When
        String token = jwtUtil.generateToken(username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Validate valid token")
    void isValidToken_ValidToken_ReturnsTrue() {
        // Given
        String username = "test@example.com";
        String token = jwtUtil.generateToken(username);

        // When
        boolean isValid = jwtUtil.isValidToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Invalidate expired token")
    void isValidToken_ExpiredToken_ReturnsFalse() {
        // Given
        String username = "test@example.com";
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L); // Expired
        String expiredToken = jwtUtil.generateToken(username);

        // When
        boolean isValid = jwtUtil.isValidToken(expiredToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Invalidate empty token")
    void isValidToken_EmptyToken_ReturnsFalse() {
        // Given
        String emptyToken = "";

        // When & Then
        assertFalse(jwtUtil.isValidToken(emptyToken));
    }

    @Test
    @DisplayName("Extract username from token")
    void getUserName_ValidToken_ReturnsUsername() {
        // Given
        String username = "test@example.com";
        String token = jwtUtil.generateToken(username);

        // When
        String extractedUsername = jwtUtil.getUserName(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Generate token with different usernames")
    void generateToken_DifferentUsernames_GeneratesDifferentTokens() {
        // Given
        String username1 = "user1@example.com";
        String username2 = "user2@example.com";

        // When
        String token1 = jwtUtil.generateToken(username1);
        String token2 = jwtUtil.generateToken(username2);

        // Then
        assertEquals(username1, jwtUtil.getUserName(token1));
        assertEquals(username2, jwtUtil.getUserName(token2));
    }
}
