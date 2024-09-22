package com.example.linktosync.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.linktosync.Tokens.TokenRepository;
import com.example.linktosync.Tokens.TokenType;
import com.example.linktosync.Tokens.Tokens;
import com.example.linktosync.dto.LoginUserDto;
import com.example.linktosync.dto.RegisterUserDto;
import com.example.linktosync.dto.VerifyUserDto;
import com.example.linktosync.model.UnVerfiedUser;
import com.example.linktosync.model.User;
import com.example.linktosync.reponses.AuthenticationResponse;
import com.example.linktosync.reponses.LoginResponse;
import com.example.linktosync.reponses.RefreshTokenResponse;
import com.example.linktosync.repository.UnVerfiedUserRepository;
import com.example.linktosync.repository.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final UnVerfiedUserRepository unVerfiedUserRepository;

    public AuthenticationService(
        UserRepository userRepository,
        AuthenticationManager authenticationManager,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        TokenRepository tokenRepository,
        EmailService emailService,
        UnVerfiedUserRepository unVerfiedUserRepository 
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.unVerfiedUserRepository = unVerfiedUserRepository;
    }

    public void signup(RegisterUserDto input) {
        UnVerfiedUser unVerfiedUser = new UnVerfiedUser()
            .setUserName(input.getUserName())
            .setUserEmail(input.getUserEmail())
            .setUserPassword(passwordEncoder.encode(input.getUserPassword()))
            .setVerificationCode(generateVerificationCode())
            .setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(1))
            .setIsVerified(false);

        sendVerificationEmail(unVerfiedUser);
        System.out.println("Saving user: " + unVerfiedUser);
        unVerfiedUserRepository.save(unVerfiedUser);
        System.out.println("User saved successfully.");
    }

    public AuthenticationResponse verifyUser(VerifyUserDto input) {
        try {
            Optional<UnVerfiedUser> optionalUnverifiedUser = unVerfiedUserRepository.findByUserEmail(input.getUserEmail());

            if (optionalUnverifiedUser.isPresent()) {
                UnVerfiedUser unverifiedUser = optionalUnverifiedUser.get();

                // Check if the verification code has expired
                if (unverifiedUser.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                    throw new RuntimeException("Verification code has expired");
                }

                // Check if the provided code matches
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
                    saveUserToken(savedUser, accessToken,refreshToken);

                    return AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
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
        } catch (RuntimeException e) {
            throw new RuntimeException("Verification process failed: " + e.getMessage());
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
        var expiredIn = jwtService.getExpirationTime();

        revokeAllUserTokens(user);
        deleteAllUserTokens(user);
        saveUserToken(user, accessToken,refreshToken);

        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(expiredIn)
            .build();
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser_UserId(user.getUserId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    private void deleteAllUserTokens(User user) {
        var tokens = tokenRepository.findAllByUser_UserId(user.getUserId());
        tokens.forEach(token -> {
            if (token.isExpired() || token.isRevoked()) {
                tokenRepository.delete(token);
            }
        });
    }

    private void saveUserToken(User user,String accessToken, String refreshToken) {
        var token = Tokens.builder()
            .user(user)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
            .tokenType(TokenType.BEARER)
            .expired(false)
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

    private void sendVerificationEmail(UnVerfiedUser unVerfiedUser) {
     
    String verificationCode =  unVerfiedUser.getVerificationCode();
    String subject = "Verification Code: " + verificationCode + " is your LinktoSync email confirmation code";
    String htmlMessage = 
            "<!DOCTYPE html>"
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
                +" <p style=\"font-size: 16px; color: #9e9e9e;\">This code will expire in 1 minute</p>"
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
            // Handle email sending exception
        }
    }


    private void reSendVerificationEmail(UnVerfiedUser unVerfiedUser) {
        String verificationCode = unVerfiedUser.getVerificationCode();
        String subject = "Verification Code: " + verificationCode + " - Please Verify Your Account"; 
        String htmlMessage = "<!DOCTYPE html>"
            + "<html><head><meta charset=\"utf-8\">"
            + "<title>Resend Verification With LinktoSync</title></head><body>"
            + "<div style=\"background-color:#ffffff;padding:20px;text-align:center;\">"
            + "<div style=\"max-width:600px;margin:auto;background-color:#000000;padding:20px;border-radius:8px;\">"
            + "<img src=\"cid:logo\" alt=\"Company Logo\" style=\"width:120px;\">"
            + "<h2 style=\"color:#03c9D7;\">Welcome to LinktoSync!</h2>"
            + "<p style=\"font-size: 16px; color: #cecaca;\">It looks like your previous verification code has expired.</p>"
        
            + "<h3 style=\"color:#fffdfd; margin-bottom:30px;\">Your New Verification Code:</h3>"
            + "<span style=\"padding:12px 24px;border-radius:4px;color:#ffffff;background:#F15A29;font-size:29px; \">" 
            + verificationCode + "</span>"
            + "<p style=\"font-size: 16px; color: #cecaca;margin-top:30px;\">This code will expire in 1 minute</p>"
            + "<p style=\"font-size: 14px; color: #9e9e9e; margin-top: 20px;\">If you didn&#39;t request this, you can safely ignore this email.</p>"
            + "<p style=\"font-size:14px;color:#cecaca;\">Thank you for using <b>LinktoSync</b>.</p>"
            + "</div></div></body></html>";
    
        try {
            emailService.sendVerificationEmail(unVerfiedUser.getUserEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            // Handle email sending exception
        }
    }
    

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
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
                .expiresIn(jwtService.getExpirationTime()) // assuming this method returns expiration time
                .build();

        return ResponseEntity.ok(responseBody);
    }

    return new ResponseEntity<>(HttpStatus.FORBIDDEN);  // in case the refresh token is not valid
}

}
