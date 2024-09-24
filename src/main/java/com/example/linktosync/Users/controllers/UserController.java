package com.example.linktosync.Users.controllers;

import com.example.linktosync.Users.dto.UserDto;
import com.example.linktosync.Tokens.services.JwtService;
import com.example.linktosync.Users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RequestMapping("/v1/linktosync/users")
@RestController
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping("/me/{userName}")
    public ResponseEntity<?> getUserByUsername(@PathVariable("userName") String userName,
                                               @RequestHeader("Authorization") String tokenHeader) {

        System.out.println(userName);
        System.out.println(tokenHeader);


        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);



            boolean tokenValid = jwtService.isTokenValidForMe(token ,userName);
            if (tokenValid) {
                UserDto userDto = userService.getUserByUsername(userName);
                return ResponseEntity.ok(userDto);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is invalid or expired");
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Authorization header is missing or malformed");
    }
}




