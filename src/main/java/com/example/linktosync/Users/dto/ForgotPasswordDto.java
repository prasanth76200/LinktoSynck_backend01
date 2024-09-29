package com.example.linktosync.Users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ForgotPasswordDto {

    @NotBlank(message = "Email address is required.")
    @Email(message = "Please provide a valid email address.")
    private String userEmail;

    
}
