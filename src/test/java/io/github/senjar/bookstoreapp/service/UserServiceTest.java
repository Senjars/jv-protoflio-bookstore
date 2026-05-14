package io.github.senjar.bookstoreapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.github.senjar.bookstoreapp.dto.user.UserRegistrationRequestDto;
import io.github.senjar.bookstoreapp.dto.user.UserResponseDto;
import io.github.senjar.bookstoreapp.exception.RegistrationException;
import io.github.senjar.bookstoreapp.mapper.UserMapper;
import io.github.senjar.bookstoreapp.model.user.Role;
import io.github.senjar.bookstoreapp.model.user.RoleName;
import io.github.senjar.bookstoreapp.model.user.User;
import io.github.senjar.bookstoreapp.repository.user.RoleRepository;
import io.github.senjar.bookstoreapp.repository.user.UserRepository;
import io.github.senjar.bookstoreapp.service.impl.UserServiceImpl;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Register user successfully")
    void register_ValidRequest_ReturnsUserResponseDto() throws RegistrationException {
        // Given
        String email = "test@example.com";
        String password = "password";
        String encodedPassword = "encodedPassword";

        UserRegistrationRequestDto requestDto = createUserRegistrationRequestDto(email, password);

        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setShippingAddress("Address");

        Role userRole = new Role();
        userRole.setName(RoleName.ROLE_USER);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(email);
        savedUser.setPassword(encodedPassword);
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setShippingAddress("Address");
        savedUser.setRoles(Set.of(userRole));

        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setId(1L);
        expectedResponse.setEmail(email);
        expectedResponse.setFirstName("John");
        expectedResponse.setLastName("Doe");
        expectedResponse.setShippingAddress("Address");

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userMapper.toEntity(requestDto)).thenReturn(user);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toResponseDto(savedUser)).thenReturn(expectedResponse);

        // When
        UserResponseDto response = userService.register(requestDto);

        // Then
        assertEquals(expectedResponse, response);
    }

    @Test
    @DisplayName("Register user with existing email throws exception")
    void register_ExistingEmail_ThrowsRegistrationException() {
        // Given
        String email = "existing@example.com";
        UserRegistrationRequestDto requestDto = createUserRegistrationRequestDto(email, "password");

        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        assertThrows(RegistrationException.class,
                () -> userService.register(requestDto));
    }

    @Test
    @DisplayName("Register user when role not found throws exception")
    void register_RoleNotFound_ThrowsRegistrationException() {
        // Given
        String email = "test@example.com";
        String password = "password";
        UserRegistrationRequestDto requestDto = createUserRegistrationRequestDto(email, password);

        User user = new User();
        user.setEmail(email);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userMapper.toEntity(requestDto)).thenReturn(user);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RegistrationException.class,
                () -> userService.register(requestDto));
    }

    private UserRegistrationRequestDto createUserRegistrationRequestDto(
            String email, String password) {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail(email);
        requestDto.setPassword(password);
        requestDto.setRepeatPassword(password);
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setShippingAddress("Address");
        return requestDto;
    }
}
