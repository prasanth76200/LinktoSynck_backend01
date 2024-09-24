package com.example.linktosync.Users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor

public class LoginUserDto {
    
    @NotBlank(message = "User name is required.")
    @Size(max = 255, message = "User name cannot exceed 255 characters.")
    private String userName;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    private String userPassword;
}
