package com.example.linktosync.Users.services;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface  LogoutService {

    public ResponseEntity<?> logout(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication);
    
}
