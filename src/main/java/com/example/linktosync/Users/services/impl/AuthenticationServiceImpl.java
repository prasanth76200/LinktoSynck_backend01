package com.example.linktosync.Users.services.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import com.example.linktosync.Emails.services.EmailService;
import com.example.linktosync.Tokens.responses.RefreshTokenResponse;
import com.example.linktosync.Tokens.services.JwtService;
import com.example.linktosync.Users.services.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.linktosync.Tokens.repository.TokenRepository;
import com.example.linktosync.Tokens.models.TokenType;
import com.example.linktosync.Tokens.models.Tokens;
import com.example.linktosync.Users.dto.LoginUserDto;
import com.example.linktosync.Users.dto.RegisterUserDto;
import com.example.linktosync.Users.dto.VerifyUserDto;
import com.example.linktosync.Users.models.UnVerfiedUser;
import com.example.linktosync.Users.models.User;
import com.example.linktosync.Users.response.AuthenticationResponse;
import com.example.linktosync.Users.response.LoginResponse;
import com.example.linktosync.Users.repository.UnVerfiedUserRepository;
import com.example.linktosync.Users.repository.UserRepository;

import jakarta.mail.MessagingException;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final UnVerfiedUserRepository unVerfiedUserRepository;

    public void signup(RegisterUserDto input) {
        UnVerfiedUser unVerfiedUser = new UnVerfiedUser()
                .setUserName(input.getUserName())
                .setUserEmail(input.getUserEmail())
                .setUserPassword(passwordEncoder.encode(input.getUserPassword()))
                .setVerificationCode(generateVerificationCode())
                .setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(1))
                .setIsVerified(false);

        sendVerificationEmail(unVerfiedUser);
        unVerfiedUserRepository.save(unVerfiedUser);
    }

    public AuthenticationResponse verifyUser(VerifyUserDto input) {
        Optional<UnVerfiedUser> optionalUnverifiedUser = unVerfiedUserRepository.findByUserEmail(input.getUserEmail());

        if (optionalUnverifiedUser.isPresent()) {
            UnVerfiedUser unverifiedUser = optionalUnverifiedUser.get();

            if (unverifiedUser.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired");
            }

            if (unverifiedUser.getVerificationCode().equals(input.getVerificationCode())) {
                User verifiedUser = new User()
                        .setUserName(unverifiedUser.getUserName())
                        .setUserEmail(unverifiedUser.getUserEmail())
                        .setUserPassword(unverifiedUser.getUserPassword())
                        .setEnabled(true);

                try {
                    userRepository.save(verifiedUser);
                } catch (DataIntegrityViolationException e) {
                    throw new RuntimeException("User with this email or username already exists");
                }

                unVerfiedUserRepository.delete(unverifiedUser);
                var savedUser = userRepository.save(verifiedUser);
                var accessToken = jwtService.generateToken(verifiedUser);
                var refreshToken = jwtService.generateRefreshToken(verifiedUser);

                saveUserToken(savedUser, accessToken, refreshToken);

                return AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .accessTokenExpiresIn(jwtService.getAccessTokenExpirationTime())
                        .refreshTokenExpiresIn(jwtService.getRefreshTokenExpirationTime())
                        .userId(savedUser.getUserId())
                        .userName(savedUser.getUsername())
                        .userEmail(savedUser.getUserEmail())
                        .build();
            } else {
                throw new RuntimeException("Invalid verification code");
            }
        } else {
            throw new RuntimeException("You are already a verified user!");
        }
    }

    public LoginResponse authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.getUserName(), input.getUserPassword())
        );

        User user = userRepository.findByUserName(input.getUserName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified. Please verify your account.");
        }

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, accessToken, refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(jwtService.getAccessTokenExpirationTime())
                .refreshTokenExpiresIn(jwtService.getRefreshTokenExpirationTime())
                .build();
    }

    public void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser_UserId(user.getUserId());
        if (!validUserTokens.isEmpty()) {
            validUserTokens.forEach(token -> {
                token.setAccessTokenExpired(true);
                token.setRevoked(true);
            });
            tokenRepository.saveAll(validUserTokens);
        }
    }
    // New method to delete all user tokens
    public void deleteAllUserTokens(User user) {
        var tokens = tokenRepository.findAllByUser_UserId(user.getUserId());
        tokens.forEach(token -> {
            if (token.isAccessTokenExpired() || token.isRevoked()) {
                tokenRepository.delete(token);
            }
        });
    }


    public void saveUserToken(User user, String accessToken, String refreshToken) {
        Tokens token = Tokens.builder()
                .user(user)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TokenType.BEARER)
                .accessTokenExpired(false)
                .refreshTokenExpired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    public void resendVerificationCode(String email) {
        Optional<UnVerfiedUser> optionalUser = unVerfiedUserRepository.findByUserEmail(email);
        if (optionalUser.isPresent()) {
            UnVerfiedUser unVerifiedUser = optionalUser.get();
            unVerifiedUser.setVerificationCode(generateVerificationCode());
            unVerifiedUser.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(1));
            reSendVerificationEmail(unVerifiedUser);
            unVerfiedUserRepository.save(unVerifiedUser);
        } else {
            throw new RuntimeException("Your account is already verified. Please log in with your username and password.");
        }
    }

    public void sendVerificationEmail(UnVerfiedUser unVerfiedUser) {
        String verificationCode = unVerfiedUser.getVerificationCode();
        String subject = "Verification Code: " + verificationCode + " is your LinktoSync email confirmation code";
        String htmlMessage = "<!DOCTYPE html>"
                + "<head>"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<title>Verify With LinktoSync</title>"
                + "</head>"
                + "<html>"
                + "<body style=\"font-family: Helvetica, Arial, sans-serif; margin: 0; padding: 0;\">"
                + "<div style=\" background-color: #ffffff; padding: 20px; text-align: center;\">"
                + "<div style=\"max-width: 600px; margin: auto; background-color: #000000; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<img src=\"cid:logo\" alt=\"Company Logo\" style=\"width: 150px; \">"
                + "<h2 style=\"color: #03c9D7; \">Welcome to LinktoSync!</h2>"
                + "<p style=\"font-size: 16px; color: #9e9e9e;\">Here&#39;s your verification code to complete the sign-up process.</p>"
                + "<p style=\"font-size: 16px; color: #9e9e9e;\">This code will expire in 1 minute</p>"
                + "<h3 style=\"color:#fffdfd;\">Your Verification Code:</h3>"
                + "<span style=\"padding: 12px 24px; border-radius: 4px; color: #ffffff; background: #F15A29;display: inline-block;text-align: center; font-weight: bold; font-size: 29px;\">"
                + verificationCode + "</span>"
                + "<p style=\"font-size: 14px; color: #9e9e9e; margin-top: 20px;\">If you didn&#39;t request this, you can safely ignore this email.</p>"
                + "<p style=\"font-size: 14px; color: #9e9e9e;\">Thank you for being a part of <a href=\"https://linktosync.com\" style=\"color: #F15A29;\"><b>LinktoSync</b></a></p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(unVerfiedUser.getUserEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending verification email: " + e.getMessage());
        }
    }

    public void reSendVerificationEmail(UnVerfiedUser unVerfiedUser) {
        String verificationCode = unVerfiedUser.getVerificationCode();
        String subject = "New Verification Code: " + verificationCode + " is your LinktoSync email confirmation code";
        String htmlMessage = "<!DOCTYPE html>"
                + "<head>"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<title>Verify With LinktoSync</title>"
                + "</head>"
                + "<html>"
                + "<body style=\"font-family: Helvetica, Arial, sans-serif; margin: 0; padding: 0;\">"
                + "<div style=\" background-color: #ffffff; padding: 20px; text-align: center;\">"
                + "<div style=\"max-width: 600px; margin: auto; background-color: #000000; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<img src=\"cid:logo\" alt=\"Company Logo\" style=\"width: 150px; \">"
                + "<h2 style=\"color: #03c9D7; \">Welcome to LinktoSync!</h2>"
                + "<p style=\"font-size: 16px; color: #9e9e9e;\">Here&#39;s your new verification code to complete the sign-up process.</p>"
                + "<p style=\"font-size: 16px; color: #9e9e9e;\">This code will expire in 1 minute</p>"
                + "<h3 style=\"color:#fffdfd;\">Your Verification Code:</h3>"
                + "<span style=\"padding: 12px 24px; border-radius: 4px; color: #ffffff; background: #F15A29;display: inline-block;text-align: center; font-weight: bold; font-size: 29px;\">"
                + verificationCode + "</span>"
                + "<p style=\"font-size: 14px; color: #9e9e9e; margin-top: 20px;\">If you didn&#39;t request this, you can safely ignore this email.</p>"
                + "<p style=\"font-size: 14px; color: #9e9e9e;\">Thank you for being a part of <a href=\"https://linktosync.com\" style=\"color: #F15A29;\"><b>LinktoSync</b></a></p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(unVerfiedUser.getUserEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending verification email: " + e.getMessage());
        }
    }

    public String generateVerificationCode() {
        return String.valueOf(new Random().nextInt(999999));
    }



    public ResponseEntity<?> toRefreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        // extract the token from authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String oldRefreshToken = authHeader.substring(7);

        // extract username from token
        String username = jwtService.extractUsername(oldRefreshToken);

        // check if the user exists in the database
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("No user found"));

        // check if the token is valid
        if (jwtService.isValidRefreshToken(oldRefreshToken, user)) {
            // generate new access and refresh tokens
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            revokeAllUserTokens(user);
            deleteAllUserTokens(user);
            saveUserToken(user, accessToken,refreshToken);

            // Create a RefreshTokenResponse and return it
            RefreshTokenResponse responseBody = RefreshTokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .accessTokenExpiresIn(jwtService.getAccessTokenExpirationTime())
                    .refreshTokenExpiresIn(jwtService.getRefreshTokenExpirationTime())
                    // assuming this method returns expiration time
                    .build();

            return ResponseEntity.ok(responseBody);
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);  // in case the refresh token is not valid
    }
}
//Optional<Tokens> oldToken = tokenRepository.findByRefreshToken(oldRefreshToken);

