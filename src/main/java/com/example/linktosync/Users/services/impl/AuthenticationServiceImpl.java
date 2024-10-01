package com.example.linktosync.Users.services.impl;

import java.time.LocalDateTime;
import java.util.Date;
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

import com.example.linktosync.Emails.services.EmailService;
import com.example.linktosync.Tokens.models.TokenType;
import com.example.linktosync.Tokens.models.Tokens;
import com.example.linktosync.Tokens.repository.TokenRepository;
import com.example.linktosync.Tokens.responses.RefreshTokenResponse;
import com.example.linktosync.Tokens.services.JwtService;
import com.example.linktosync.Users.dto.LoginUserDto;
import com.example.linktosync.Users.dto.RegisterUserDto;
import com.example.linktosync.Users.dto.VerifyUserDto;
import com.example.linktosync.Users.models.UnVerfiedUser;
import com.example.linktosync.Users.models.User;
import com.example.linktosync.Users.repository.UnVerfiedUserRepository;
import com.example.linktosync.Users.repository.UserRepository;
import com.example.linktosync.Users.response.AuthenticationResponse;
import com.example.linktosync.Users.response.LoginResponse;
import com.example.linktosync.Users.services.AuthenticationService;
import com.example.linktosync.exceptions.AccountNotVerifiedException;
import com.example.linktosync.exceptions.EmailSendingException;
import com.example.linktosync.exceptions.InvalidVerificationCodeException;
import com.example.linktosync.exceptions.UserAlreadyExistsException;
import com.example.linktosync.exceptions.UserAlreadyVerifiedException;
import com.example.linktosync.exceptions.UserNotFoundException;
import com.example.linktosync.exceptions.VerificationCodeExpiredException;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

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
    
    // private final LogoutService logoutService;



 @Override
    public void signup(RegisterUserDto input) {
        UnVerfiedUser existingUser = unVerfiedUserRepository.findByUserEmail(input.getUserEmail()).orElse(null);
         if (existingUser != null) {
            throw new UserAlreadyExistsException("User with this email already exists.");
        }
        UnVerfiedUser unVerfiedUser = new UnVerfiedUser()
                .setUserName(input.getUserName())
                .setUserEmail(input.getUserEmail())
                .setRole(input.getRole())
                .setUserPassword(passwordEncoder.encode(input.getUserPassword()))
                .setVerificationCode(generateVerificationCode())
                .setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(1))
                .setIsVerified(false);

        sendVerificationEmail(unVerfiedUser);
        unVerfiedUserRepository.save(unVerfiedUser);
    }
    
    @Override
    public AuthenticationResponse verifyUser(VerifyUserDto input) {

        
        Optional<UnVerfiedUser> optionalUnverifiedUser = unVerfiedUserRepository.findByUserEmail(input.getUserEmail());

        if (optionalUnverifiedUser.isPresent()) {
            UnVerfiedUser unverifiedUser = optionalUnverifiedUser.get();

           
            if (unverifiedUser.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new VerificationCodeExpiredException("Verification code has expired.");
            }

            if (unverifiedUser.getVerificationCode().equals(input.getVerificationCode())) {
                User verifiedUser = new User()
                        .setUserName(unverifiedUser.getUserName())
                        .setUserEmail(unverifiedUser.getUserEmail())
                        .setUserPassword(unverifiedUser.getUserPassword())
                        .setRole(unverifiedUser.getRole())
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
                        .role(savedUser.getRole())
                        .build();
                        
            } else {
                throw new InvalidVerificationCodeException("Invalid verification code.");
            }
        } else {
               throw new UserAlreadyVerifiedException("You are already a verified user!");
        }
    }
    @Override
    public LoginResponse authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.getUserName(), input.getUserPassword())
        );

        User user = userRepository.findByUserName(input.getUserName())
                 .orElseThrow(() -> new UserNotFoundException("User not found."));

        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Account not verified. Please verify your account.");
        }

        var accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

       


        revokeAllUserTokens(user);
        deleteAllUserTokens(user);
        saveUserToken(user, accessToken, refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(jwtService.getAccessTokenExpirationTime())
                .refreshTokenExpiresIn(jwtService.getRefreshTokenExpirationTime())
                .build();
    }



    @Override
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
    @Override
    // New method to delete all user tokens
    public void deleteAllUserTokens(User user) {
        var tokens = tokenRepository.findAllByUser_UserId(user.getUserId());
        tokens.forEach(token -> {
            if (token.isAccessTokenExpired() || token.isRevoked()) {
                tokenRepository.delete(token);
            }
        });
    }

    @Override
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

    
    @Override
    public void resendVerificationCode(String email) {
        Optional<UnVerfiedUser> optionalUser = unVerfiedUserRepository.findByUserEmail(email);
        if (optionalUser.isPresent()) {
            UnVerfiedUser unVerifiedUser = optionalUser.get();
            unVerifiedUser.setVerificationCode(generateVerificationCode());
            unVerifiedUser.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
            reSendVerificationEmail(unVerifiedUser);
            unVerfiedUserRepository.save(unVerifiedUser);
        } else {
            throw new RuntimeException("Your account is already verified. Please log in with your username and password.");
        }
    }

 
    @Override
    public void sendVerificationEmail(UnVerfiedUser unVerfiedUser) {
        String verificationCode = unVerfiedUser.getVerificationCode();
        String userName =unVerfiedUser.getUserName();
        String subject = "Confirmation Code: " + verificationCode + "from your LinktoSync" ;
        String htmlMessage = emailService.createEmailContent(verificationCode, "Welcome to LinktoSync!", userName);
    
        try {
            emailService.sendVerificationEmail(unVerfiedUser.getUserEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
             throw new EmailSendingException("Error sending verification email.", e);
        }
    }
    
    @Override
    public void reSendVerificationEmail(UnVerfiedUser unVerfiedUser) {
        String verificationCode = unVerfiedUser.getVerificationCode();
        String userName =unVerfiedUser.getUserName();
        String subject = "Re-sent Confirmation Code: " + verificationCode + " from your LinktoSync";
        String htmlMessage =emailService.createEmailContent(verificationCode, "Welcome to LinktoSync!", userName);
    
        try {
            emailService.sendVerificationEmail(unVerfiedUser.getUserEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new EmailSendingException("Error sending verification email.", e);
        }
    }

    
    @Override
    public String generateVerificationCode() {
        return String.valueOf(new Random().nextInt(999999));
    }


    @Override
    public ResponseEntity<?> toRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        // Extract the token from the Authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String oldRefreshToken = authHeader.substring(7);
    
        // Extract username from token
        String username = jwtService.extractUsername(oldRefreshToken);
        Date refreshTokenExpiration = jwtService.extractRefreshTokenExpiration(oldRefreshToken);
    
        // Check if the user exists in the database
        User user = userRepository.findByUserName(username)
                .orElseThrow();

                // System.out.println(jwtService.isRefreshTokenExpire(oldRefreshToken)+ "yes");

                // if(jwtService.isRefreshTokenExpire(oldRefreshToken)){
                //     refreshTokenService.revokeRefreshToken(user.getUserId());

                // }

                if (jwtService.isValidRefreshToken(oldRefreshToken, user) && !jwtService.isRefreshTokenExpire(oldRefreshToken)) {
        // Check if the refresh token is valid and not expired

            System.out.println("ckeck from local");
            // Generate new access token (and optionally refresh token)
            String accessToken = jwtService.generateToken(user);
    
            // Optionally rotate the refresh token
            // String newRefreshToken = jwtService.generateRefreshToken(user);
    
            // Revoke and save tokens
            revokeAllUserTokens(user);
            deleteAllUserTokens(user);
            saveUserToken(user, accessToken, oldRefreshToken);  // Or newRefreshToken
    
            // Create a response body with tokens and expiration times
            RefreshTokenResponse responseBody = RefreshTokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(oldRefreshToken)  // Or newRefreshToken
                    .accessTokenExpiresIn(jwtService.getAccessTokenExpirationTime())
                    .refreshTokenExpiresIn(refreshTokenExpiration.getTime())
                    .build();
    
            return ResponseEntity.ok(responseBody);
        }
    
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);  // Invalid or expired refresh token
    }

 

}
//Optional<Tokens> oldToken = tokenRepository.findByRefreshToken(oldRefreshToken);

