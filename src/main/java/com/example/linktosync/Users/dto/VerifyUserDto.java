package com.example.linktosync.Users.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor

@RequiredArgsConstructor
public class VerifyUserDto {
    @NotBlank(message = "Email address is required.")
    @Email(message = "Please provide a valid email address.")
    private String userEmail;

    @NotBlank(message = "Verification code is required.")
    private String verificationCode;

    private LocalDateTime setVerificationCodeExpiresAt = LocalDateTime.now().plusMinutes(5); 
    
}
