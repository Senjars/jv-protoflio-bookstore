package io.github.senjar.bookstoreapp.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.senjar.bookstoreapp.dto.user.UserLoginRequestDto;
import io.github.senjar.bookstoreapp.dto.user.UserLoginResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication Service Tests")
class AuthenticationServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Authenticate user successfully")
    void authenticate_ValidCredentials_ReturnsToken() {
        // Given
        String email = "test@example.com";
        String password = "password";
        String expectedToken = "mockToken";
        UserLoginRequestDto requestDto = new UserLoginRequestDto(email, password);

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getName()).thenReturn(email);
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)))
                .thenReturn(mockAuthentication);
        when(jwtUtil.generateToken(email)).thenReturn(expectedToken);

        // When
        UserLoginResponseDto response = authenticationService.authenticate(requestDto);

        // Then
        assertEquals(expectedToken, response.token());
    }

    @Test
    @DisplayName("Authenticate user with invalid credentials throws exception")
    void authenticate_InvalidCredentials_ThrowsBadCredentialsException() {
        // Given
        String email = "test@example.com";
        String password = "wrongpassword";
        UserLoginRequestDto requestDto = new UserLoginRequestDto(email, password);

        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class,
                () -> authenticationService.authenticate(requestDto));
    }
}

