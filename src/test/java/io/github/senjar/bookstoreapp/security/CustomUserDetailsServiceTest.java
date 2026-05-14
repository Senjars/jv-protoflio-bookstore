package io.github.senjar.bookstoreapp.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.github.senjar.bookstoreapp.model.user.Role;
import io.github.senjar.bookstoreapp.model.user.RoleName;
import io.github.senjar.bookstoreapp.model.user.User;
import io.github.senjar.bookstoreapp.repository.user.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Custom User Details Service Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Load user by username successfully")
    void loadUserByUsername_ValidEmail_ReturnsUserDetails() {
        // Given
        String email = "test@example.com";

        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName(RoleName.ROLE_USER);

        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword("encodedPassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRoles(Set.of(userRole));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertEquals(email, result.getUsername());
        assertEquals(user.getPassword(), result.getPassword());
    }

    @Test
    @DisplayName("Load user by username throws exception when user not found")
    void loadUserByUsername_InvalidEmail_ThrowsUsernameNotFoundException() {
        // Given
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(email));
    }
}
