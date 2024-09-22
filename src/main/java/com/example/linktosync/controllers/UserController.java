package com.example.linktosync.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.linktosync.dto.UserDto;
import com.example.linktosync.services.JwtService;
import com.example.linktosync.services.UserService;

import lombok.RequiredArgsConstructor;




@RestController
@RequestMapping("v1/lintosync/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

   @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(UUID userId) {
        UserDto userDto = userService.getUserById(userId);
        // if(jwtService.validateToken(token, user))
        return ResponseEntity.ok(userDto);

        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // User currentUser = (User) authentication.getPrincipal();


        // return ResponseEntity.ok(currentUser);

    }
    


    // @GetMapping
    // public ResponseEntity<List<UserDto>> getAllUsers() {

        
    //     List<User> users = userService.getAllUsers();

    //     // Convert User entities to UserDto
    //     List<UserDto> userDtos = users.stream()
    //             .map(user -> {
    //                 UserDto dto = new UserDto();
    //                 dto.setId(user.getUserId()); // Assuming `getId()` returns UUID
    //                 dto.setUserEmail(user.getUserEmail());
    //                 dto.setUserName(user.getUsername());
    //                 return dto;
    //             })
    //             .collect(Collectors.toList());

    //     return ResponseEntity.ok(userDtos);
    // }
}
