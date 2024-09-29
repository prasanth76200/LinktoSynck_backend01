package com.example.linktosync.Users.services.impl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.linktosync.Emails.services.EmailService;
import com.example.linktosync.Tokens.models.Tokens;
import com.example.linktosync.Tokens.repository.TokenRepository;
import com.example.linktosync.Tokens.services.JwtService;
import com.example.linktosync.Users.dto.ResetPasswordDto;
import com.example.linktosync.Users.models.User;
import com.example.linktosync.Users.repository.UserRepository;
import com.example.linktosync.Users.services.AuthenticationService;
import com.example.linktosync.Users.services.ForgotPasswordService;
import com.example.linktosync.exceptions.EmailSendingException;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String requestForgotPasswordReset(String email) {
        System.out.println("Service: " + email);

        // Find the user by email
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // Generate a new token for the user
        String newToken = jwtService.generateToken(user);
        

        // Fetch the existing tokens for the user
        List<Tokens> tokensList = tokenRepository.findAllValidTokenByUser(user.getUserId());

        if (!tokensList.isEmpty()) {
            // Update the existing valid access token
            Tokens existingToken = tokensList.get(0);
            existingToken.setAccessToken(newToken);
            existingToken.setAccessTokenExpired(false);
            System.out.println("Updated Access Token: " + existingToken.getAccessToken());
            tokenRepository.save(existingToken);
        } else {
            // Create a new token
            Tokens token = new Tokens();
            token.setUser(user);
            token.setAccessToken(newToken);
            token.setAccessTokenExpired(false);
            token.setRefreshTokenExpired(false);
            token.setRevoked(false);
            System.out.println("Created New Access Token: " + token.getAccessToken());
            tokenRepository.save(token);
        }

        String resetLink = "http://localhost:8080/v1/linktosync/auth/reset-password?token=" + newToken;
        System.out.println(resetLink);
        String subject = "Alter Password Reset Request for Your LinktoSync Account";
        String htmlMessage = emailService.createForgotPasswordEmail(user.getUsername(), resetLink);

        try {
            emailService.sendVerificationEmail(user.getUserEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new EmailSendingException("Error sending verification email.", e);
        }
       
        return newToken;
    }

    @Override
public String resetPassword(String token, ResetPasswordDto resetPasswordDto) {
    

    // Validate the email associated with the reset request
    String userEmail = resetPasswordDto.getUserEmail();
    User user = userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

    Tokens currentToken = tokenRepository.findByAccessToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid token."));

    if (jwtService.isTokenExpired(token) || currentToken.isAccessTokenExpired()) {
        return "Token expired";
    }

    // Validate if the user is enabled
    if (!user.isEnabled()) {
        return "Invalid token.";
    }

    // Encrypt the new password
    String encryptedPassword = passwordEncoder.encode(resetPasswordDto.getNewUserPassword());

    // Set the new password for the user
    user.setUserPassword(encryptedPassword);
    userRepository.save(user); // Save updated user

    // Invalidate the current token
    currentToken.setAccessTokenExpired(true);
    currentToken.setRefreshTokenExpired(true);
    currentToken.setRevoked(true);
    tokenRepository.save(currentToken); // Save updated token status

    return "Your password has been successfully updated.";
}

    
}
