package com.example.linktosync.Users.services;

import com.example.linktosync.Users.dto.ResetPasswordDto;

public interface ForgotPasswordService {
        public String requestForgotPasswordReset(String email);
        public String resetPassword(String token, ResetPasswordDto resetPasswordDto);
}
