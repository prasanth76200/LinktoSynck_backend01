package com.example.linktosync.Users.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;


// import org.springframework.ui.Model;


@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/linktosync/users")
public class UserController {

   @GetMapping("/")
    public String getMethodName() {
        return "Hello";
    }
  
   // private final UserService userService;
      // private final JwtService jwtService;
      // private final TokenService tokenService;
   //    private Toke
   


}
