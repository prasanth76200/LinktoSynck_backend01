package com.example.linktosync.configs;
import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.linktosync.Tokens.TokenRepository;
import com.example.linktosync.model.User;
import com.example.linktosync.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;


@Component

@AllArgsConstructor

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    // public JwtAuthenticationFilter(
    //     JwtService jwtService,
    //     UserDetailsService userDetailsService,
    //     HandlerExceptionResolver handlerExceptionResolver
    // ) {
    //     this.jwtService = jwtService;
    //     this.userDetailsService = userDetailsService;
    //     this.handlerExceptionResolver = handlerExceptionResolver;
    //     this.tokenRepository = tokenRepository;
    // }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
    
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
    
        final String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUserEmail(jwt);
        final String userName = jwtService.extractUsername(jwt);
    
        // Get Authentication from SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
        if ((userEmail != null || userName != null) && authentication == null) {
            UserDetails userDetails;
            if (userEmail != null) {
                userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            } else {
                userDetails = this.userDetailsService.loadUserByUsername(userName);
            }
    
            // Check if token is valid, not expired, and belongs to the current user
            var isTokenValid = tokenRepository.findByAccessToken(jwt)
                    .map(t -> !t.isExpired() && !t.isRevoked())
                    .orElse(false);
    
            // Call validateToken to ensure token belongs to the current user
            jwtService.validateToken(jwt, (User) userDetails);
    
            if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
    
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
    
        filterChain.doFilter(request, response);
    }

}