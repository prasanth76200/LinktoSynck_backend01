package com.example.linktosync.configs;
import java.io.IOException;

import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.linktosync.Tokens.repository.TokenRepository;
import com.example.linktosync.Tokens.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;


@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Lazy
    private final JwtService jwtService;
    @Lazy
    private final UserDetailsService userDetailsService;
    @Lazy
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
    // System.out.println("Yes");
    // System.out.println(authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
    
        final String jwt = authHeader.substring(7);
       // System.out.println(jwt);
        // final String userEmail = jwtService.extractUserEmail(jwt);
        final String userName = jwtService.extractUsername(jwt);
    
        // Get Authentication from SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       // System.out.println(authentication);
        if (userName != null && authentication == null) {
           
             UserDetails userDetails = this.userDetailsService.loadUserByUsername(userName);
       
            var isTokenValidByAccessToken = tokenRepository.findByAccessToken(jwt)
                    .map(t -> !t.isAccessTokenExpired()  && !t.isRevoked())
                    .orElse(false);
                    System.out.println(isTokenValidByAccessToken);
            
            // System.out.println("jwtService" +jwtService.isTokenValid(jwt, userDetails));

            if (jwtService.isTokenValid(jwt, userDetails) && isTokenValidByAccessToken) {
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