package io.github.senjar.bookstoreapp.service;

import io.github.senjar.bookstoreapp.dto.user.UserRegistrationRequestDto;
import io.github.senjar.bookstoreapp.dto.user.UserResponseDto;
import io.github.senjar.bookstoreapp.exception.RegistrationException;

public interface UserService {

    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;
}
