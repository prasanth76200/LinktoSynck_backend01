package com.example.linktosync.Users.controllers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.linktosync.Tokens.services.JwtService;
import com.example.linktosync.Users.dto.LoginUserDto;
import com.example.linktosync.Users.dto.RegisterUserDto;
import com.example.linktosync.Users.dto.ResetPasswordDto;
import com.example.linktosync.Users.dto.VerifyUserDto;
import com.example.linktosync.Users.services.AuthenticationService;
import com.example.linktosync.Users.services.ForgotPasswordService;
import com.example.linktosync.exceptions.UserAlreadyExistsException;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;


@RequestMapping("v1/linktosync/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;
    
   private final ForgotPasswordService forgotPasswordService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService,ForgotPasswordService forgotPasswordService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.forgotPasswordService = forgotPasswordService;
    }

 

    
    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserDto registerUserDto ) {
        try {
            authenticationService.signup(registerUserDto);
         
            // return "redirect:/user/welcome"; 
            return ResponseEntity.ok("Registration successful! Please check your email to verify your account.");
        } catch (UserAlreadyExistsException ex) {
            // Handle the custom exception for existing users
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: " + ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            // Handle SQL constraint violations
            String errorMessage = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "Data Integrity Violation";
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: " + errorMessage);
        } catch (Exception e) {
            // Handle any unexpected exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
    


    @PostMapping("/login")
    public ResponseEntity<String> authenticate(@Valid @RequestBody LoginUserDto loginUserDto, HttpServletResponse response) {
        return ResponseEntity.ok(authenticationService.authenticate(loginUserDto, response));
    }



  

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@Valid @RequestBody VerifyUserDto verifyUserDto, HttpServletResponse response) {
        try {
            // AuthenticationResponse response = authenticationService.verifyUser(verifyUserDto);
            return ResponseEntity.ok(authenticationService.verifyUser(verifyUserDto,response));
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

    @PostMapping("/forgot")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
    // Log the incoming request
    System.out.println("Received forgot password request for: " + email);

    if (email == null) {
        return ResponseEntity.badRequest().body("Email address is required.");
    }

   //  Call the service method
    String newToken = forgotPasswordService.requestForgotPasswordReset(email);

    return ResponseEntity.ok(newToken);
}

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token,
                                             @RequestBody ResetPasswordDto resetPasswordDto) {
    System.out.println("Received token: " + token);
    System.out.println("Received email: " + resetPasswordDto.getUserEmail());
    System.out.println("New user password: " + resetPasswordDto.getNewUserPassword());
    
    forgotPasswordService.resetPassword(token, resetPasswordDto);
    // Implement your logic to validate the token and reset the password

    return ResponseEntity.ok("Password reset successfully.");
}

}