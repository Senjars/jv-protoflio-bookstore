package io.github.senjar.bookstoreapp.mapper;

import io.github.senjar.bookstoreapp.dto.user.UserRegistrationRequestDto;
import io.github.senjar.bookstoreapp.dto.user.UserResponseDto;
import io.github.senjar.bookstoreapp.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toResponseDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserRegistrationRequestDto userRegistrationRequestDto);
}
