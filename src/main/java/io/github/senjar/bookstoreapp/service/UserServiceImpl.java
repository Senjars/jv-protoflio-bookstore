package io.github.senjar.bookstoreapp.service;

import io.github.senjar.bookstoreapp.dto.user.UserRegistrationRequestDto;
import io.github.senjar.bookstoreapp.dto.user.UserResponseDto;
import io.github.senjar.bookstoreapp.exception.RegistrationException;
import io.github.senjar.bookstoreapp.mapper.UserMapper;
import io.github.senjar.bookstoreapp.model.User;
import io.github.senjar.bookstoreapp.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("Can't register");
        }

        User user = userMapper.toModel(requestDto);
        User savedUser = userRepository.save(user);

        return userMapper.toResponseDto(savedUser);
    }
}
