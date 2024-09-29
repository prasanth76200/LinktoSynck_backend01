package com.example.linktosync.Users.services.impl;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import com.example.linktosync.Tokens.models.Tokens;
import com.example.linktosync.Tokens.repository.TokenRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogoutServiceImpl implements LogoutHandler {

    private final TokenRepository tokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader("Authorization");
        String token;

        // Check if the Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Set status to 401 for unauthorized
            try {
                response.getWriter().write("Authorization token is missing or invalid.");
                response.getWriter().flush();
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception properly
            }
            return;
        }

        // Extract token from the Authorization header
        token = authHeader.substring(7);
        Optional<Tokens> storedToken = tokenRepository.findByAccessToken(token);

        // Check if the token exists in the database
        if (storedToken.isPresent()) {
            Tokens tokenEntity = storedToken.get();
            // Revoke the token and mark it as expired
            tokenEntity.setRevoked(true);
            tokenEntity.setRefreshTokenExpired(true);
            tokenEntity.setAccessTokenExpired(true);
            
            // Save the updated token state
            tokenRepository.save(tokenEntity);

            // Clear the security context
            SecurityContextHolder.clearContext();

            // Set the response status to OK (200)
            response.setStatus(HttpServletResponse.SC_OK);
            try {
                response.getWriter().write("Logout successful. You have been logged out.");
                response.getWriter().flush();
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception properly
            }
        } else {
            // If the token is not found, respond with a 401 status
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Set status to 401 for unauthorized
            try {
                response.getWriter().write("Invalid token. Logout failed.");
                response.getWriter().flush();
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception properly
            }
        }
    }
}
