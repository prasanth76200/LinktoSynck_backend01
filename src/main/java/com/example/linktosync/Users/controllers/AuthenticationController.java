package com.example.linktosync.Users.controllers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.linktosync.Users.dto.LoginUserDto;
import com.example.linktosync.Users.dto.RegisterUserDto;
import com.example.linktosync.Users.dto.VerifyUserDto;
import com.example.linktosync.Users.response.AuthenticationResponse;
import com.example.linktosync.Users.response.LoginResponse;
import com.example.linktosync.Users.services.AuthenticationService;
import com.example.linktosync.Tokens.services.JwtService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RequestMapping("v1/linktosync/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserDto registerUserDto) {
        try {
            authenticationService.signup(registerUserDto);
            return ResponseEntity.ok("Register Completed. Please Verify your Email.");
        } catch (DataIntegrityViolationException ex) {
            // Handle SQL constraint violation
            @SuppressWarnings("null")
            String errorMessage = ex.getRootCause().getMessage(); // Extract the SQL error message
            return ResponseEntity.status(HttpStatus.CONFLICT) // HTTP 409 Conflict
                    .body("Error: " + errorMessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody LoginUserDto loginUserDto) {
        return ResponseEntity.ok(authenticationService.authenticate(loginUserDto));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@Valid @RequestBody VerifyUserDto verifyUserDto) {
        try {
            AuthenticationResponse response = authenticationService.verifyUser(verifyUserDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) throws MessagingException {
        try {
            authenticationService.resendVerificationCode(email);
            return ResponseEntity.ok("Verification code sent");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/refresh_token")
    public ResponseEntity<?> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return authenticationService.toRefreshToken(request, response);
    }

}