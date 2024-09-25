package com.example.linktosync.Users.services.impl;

import java.util.ArrayList;

import com.example.linktosync.Tokens.services.JwtService;

import com.example.linktosync.Users.services.AuthenticationService;
import com.example.linktosync.Users.services.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.linktosync.Users.dto.UserDto;
import com.example.linktosync.Users.models.User;
import com.example.linktosync.Users.repository.UserRepository;
import org.springframework.context.annotation.Lazy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
     private final AuthenticationService authenticationService;
     private final UserRepository userRepository;
     private final JwtService jwtService;

     public UserDetails loadUserByUsername(String userName){

          User user = userRepository.findByUserName(userName)
                  .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + userName));


          return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                  new ArrayList<>());

     }


     public UserDto getUserByUsername(String userName) {
          User user = userRepository.findByUserName(userName)
                  .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + userName));

          return new UserDto(user.getUserId(), user.getUsername(), user.getUserEmail());
     }

     public boolean tokenVerified (String userName, String token){


          String extractUsername = jwtService.extractUsername(token);

          return userName.equals(extractUsername) ;

     }

}
