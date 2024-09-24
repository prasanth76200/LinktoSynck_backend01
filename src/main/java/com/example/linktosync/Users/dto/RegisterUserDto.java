package com.example.linktosync.Users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RegisterUserDto {

    @NotBlank(message = "Email address is required.")
    @Email(message = "Please provide a valid email address.")
    private String userEmail;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    @Pattern(regexp = "(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}",
             message = "Password must include at least one uppercase letter, one lowercase letter, one digit, and one special character.")
    private String userPassword;

    @NotBlank(message = "User name is required.")
    @Size(max = 255, message = "User name cannot exceed 255 characters.")
    private String userName;
}
