package com.example.linktosync.Users.controllers;

import com.example.linktosync.Tokens.models.Tokens;
import com.example.linktosync.Tokens.services.TokenService;
import com.example.linktosync.Users.dto.UserDto;
import com.example.linktosync.Tokens.services.JwtService;
import com.example.linktosync.Users.repository.UserRepository;
import com.example.linktosync.Users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequiredArgsConstructor
@RequestMapping("/v1/linktosync/users")
@RestController
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final TokenService tokenService;
//    private Toke

    @GetMapping("/me/{userName}")
    public ResponseEntity<?> getUserByUsername(@PathVariable("userName") String userName,
                                               @RequestHeader("Authorization") String tokenHeader) {

        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);

            // Retrieve the token object from the database
            Optional<Tokens> tokenOptional = tokenService.getTokenByUserName(userName);

            if (tokenOptional.isPresent()) {
                Tokens databaseToken = tokenOptional.get();

                // Check if the access token matches the database token
                if (databaseToken.getAccessToken().equals(token)) {
                    String extractUsername = jwtService.extractUsername(token);
                    boolean isTokenExpired = jwtService.isTokenExpired(token);

                    // Check if the extracted username matches the path variable and if the token is not expired
                    if (extractUsername.equals(userName) && !isTokenExpired) {
                        UserDto userDto = userService.getUserByUsername(userName);
                        return ResponseEntity.ok(userDto);
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body("Token is invalid or expired");
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Your Token is not valid for this user");
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("No token found for this user");
            }

        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Authorization header is missing or malformed");
    }
}
