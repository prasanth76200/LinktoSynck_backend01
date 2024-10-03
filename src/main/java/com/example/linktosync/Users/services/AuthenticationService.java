

package com.example.linktosync.Users.services;

import org.springframework.http.ResponseEntity;

import com.example.linktosync.Users.dto.LoginUserDto;
import com.example.linktosync.Users.dto.RegisterUserDto;
import com.example.linktosync.Users.dto.VerifyUserDto;
import com.example.linktosync.Users.models.UnVerfiedUser;
import com.example.linktosync.Users.models.User;
import com.example.linktosync.Users.response.AuthenticationResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public interface AuthenticationService {
        public String signup(RegisterUserDto input);

        AuthenticationResponse verifyUser(VerifyUserDto input, HttpServletResponse response);

        public String authenticate(LoginUserDto input, HttpServletResponse response);

        void revokeAllUserTokens(User user);

        void deleteAllUserTokens(User user);

        void saveUserToken(User user, String accessToken, String refreshToken);

        void resendVerificationCode(String email);

        void sendVerificationEmail(UnVerfiedUser unVerfiedUser);

        void reSendVerificationEmail(UnVerfiedUser unVerfiedUser);

        String generateVerificationCode();


       ResponseEntity<?> toRefreshToken(
            HttpServletRequest request,
            HttpServletResponse response);

    }