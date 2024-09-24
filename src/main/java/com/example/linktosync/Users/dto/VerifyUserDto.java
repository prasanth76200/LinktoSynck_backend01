package com.example.linktosync.Users.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor

@RequiredArgsConstructor
public class VerifyUserDto {

    private String userEmail;
    private String verificationCode;
    private LocalDateTime setVerificationCodeExpiresAt = LocalDateTime.now().plusMinutes(30); 
    
}
