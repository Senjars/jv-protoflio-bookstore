package io.github.senjar.bookstoreapp.dto.user;

import io.github.senjar.bookstoreapp.validation.FieldMatch;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@FieldMatch(first = "password", second = "repeatPassword", message = "Passwords must match")
public class UserRegistrationRequestDto {

    @NotBlank
    private String email;

    @NotBlank
    @Length(min = 8, max = 26)
    private String password;

    @NotBlank
    @Length(min = 8, max = 26)
    private String repeatPassword;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String shippingAddress;

}
