package io.github.senjar.bookstoreapp.controller;

import io.github.senjar.bookstoreapp.dto.user.UserRegistrationRequestDto;
import io.github.senjar.bookstoreapp.dto.user.UserResponseDto;
import io.github.senjar.bookstoreapp.exception.RegistrationException;
import io.github.senjar.bookstoreapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final UserService userService;

    @PostMapping("/registration")
    public UserResponseDto registerUser(@RequestBody @Valid UserRegistrationRequestDto requestDto)
            throws RegistrationException {

        return userService.register(requestDto);
    }

}
