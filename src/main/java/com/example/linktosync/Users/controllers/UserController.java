package com.example.linktosync.Users.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;




@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/linktosync/users")
public class UserController {


   // private final UserService userService;
      // private final JwtService jwtService;
      // private final TokenService tokenService;
   //    private Toke
   
      @GetMapping("/me")
      public String getUserByUsername() {
   
   
       return "hello"; 
         
      }
   






}
