package com.example.linktosync.Tokens.services;

import com.example.linktosync.Tokens.models.Tokens;
import com.example.linktosync.Users.models.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.function.Function;

public interface JwtService {

    String extractUsername(String token);

    String extractUserEmail(String token);

    boolean isTokenExpired(String token);

    void validateToken(String token, User user);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    String generateToken(UserDetails userDetails);

    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

    long getAccessTokenExpirationTime();

    long getRefreshTokenExpirationTime();

    String generateRefreshToken(UserDetails userDetails);

    boolean isValidRefreshToken(String oldRefreshToken, User user);

    boolean isTokenValid(String accessToken, UserDetails userDetails);

//    boolean isTokenValidForMe(String accessToken, String userName);

    boolean isRefreshTokenExpire(String token);
}
