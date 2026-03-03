package io.github.senjar.bookstoreapp.service.impl;

import io.github.senjar.bookstoreapp.dto.user.UserRegistrationRequestDto;
import io.github.senjar.bookstoreapp.dto.user.UserResponseDto;
import io.github.senjar.bookstoreapp.exception.RegistrationException;
import io.github.senjar.bookstoreapp.mapper.UserMapper;
import io.github.senjar.bookstoreapp.model.user.Role;
import io.github.senjar.bookstoreapp.model.user.RoleName;
import io.github.senjar.bookstoreapp.model.user.User;
import io.github.senjar.bookstoreapp.repository.user.RoleRepository;
import io.github.senjar.bookstoreapp.repository.user.UserRepository;
import io.github.senjar.bookstoreapp.service.UserService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("Can't register");
        }

        User user = userMapper.toEntity(requestDto);
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RegistrationException("Can't find role by name"));

        user.setRoles(Set.of(userRole));
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        User savedUser = userRepository.save(user);

        return userMapper.toResponseDto(savedUser);
    }
}
