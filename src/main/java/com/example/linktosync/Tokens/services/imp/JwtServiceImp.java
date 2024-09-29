package com.example.linktosync.Tokens.services.imp;





import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.linktosync.Tokens.models.Tokens;
import com.example.linktosync.Tokens.repository.TokenRepository;
import com.example.linktosync.Tokens.services.JwtService;
import com.example.linktosync.Users.models.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
//@RequiredArgsConstructor
public class JwtServiceImp implements JwtService {

    private final String secretKey = "4bb6d1dfbafb64a681139d1586b6f1160d18159afd57c8c79136d7490630407c";
    private final long accessTokenExpiration = 3600000;
    private final long refreshTokenExpiration = 604800000;

//    @Value("${jwt.secret-key}")
//    private String secretKey;

//    @Value("${jwt.access-token-expiration}")
//    private long accessTokenExpiration;

//    @Value("${jwt.refresh-token-expiration}")
//    private long refreshTokenExpiration;

    private final TokenRepository tokenRepository;

//    private final UserService userService;


    public JwtServiceImp(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String extractUserEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
  
    @Override
    public Date extractRefreshTokenExpiration(String refreshToken) {
        return extractExpiration(refreshToken);
    }
    

    @Override
    public void validateToken(String token, User user) {
        String usernameFromToken = extractUsername(token);
        System.out.println("Jwt Service"+ usernameFromToken); // Extract the username or userId from the token
        if (!user.getUsername().equals(usernameFromToken)) {
            throw new IllegalStateException("Token does not belong to the current user");
        }

    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }
    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, accessTokenExpiration);
    }

    @Override
    public long getAccessTokenExpirationTime() {
        return accessTokenExpiration;
    }
    @Override
    public long getRefreshTokenExpirationTime() {
        return refreshTokenExpiration;
    }
    @Override
    public String generateRefreshToken(
            UserDetails userDetails
    ) {
        return buildToken(new HashMap<>(), userDetails, refreshTokenExpiration);
    }

    // @Override
    // public boolean isValidRefreshToken(String oldRefreshToken, User user) {
    //     String username = extractUsername(oldRefreshToken);
    //     Optional<Tokens> oldToken = tokenRepository.findByRefreshToken(oldRefreshToken);
     
    //     return (oldToken.isPresent() && oldToken.get().getRefreshToken().equals(oldRefreshToken) && username.equals(user.getUsername()) && oldToken.get().isRefreshTokenExpired() && oldToken.get().isRevoked());
    // }

    @Override
public boolean isValidRefreshToken(String oldRefreshToken, User user) {
    String username = extractUsername(oldRefreshToken);
    Optional<Tokens> oldToken = tokenRepository.findByRefreshToken(oldRefreshToken);
 
    return oldToken.isPresent() && 
           oldToken.get().getRefreshToken().equals(oldRefreshToken) && 
           username.equals(user.getUsername()) && 
           !oldToken.get().isRefreshTokenExpired() && 
           !oldToken.get().isRevoked();
}

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    @Override
    public boolean isTokenValid(String accessToken, UserDetails userDetails) {
        final String username = extractUsername(accessToken);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(accessToken);
    }

//    public boolean isTokenValidForMe(String accessToken, String userName) {
//        // Extract username from the token
//        final String tokenUsername = extractUsername(accessToken);
//
//        // Fetch UserDetails using userName
//        UserDetails userDetails = userService.loadUserByUsername(userName);
//
//        // Check if token username matches and if the token is not expired
//        return (tokenUsername.equals(userDetails.getUsername()) && !isTokenExpired(accessToken));
//    }
@Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    @Override
    public boolean isRefreshTokenExpire(String token) {
        return extractExpiration(token).before(new Date());
    }
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);

    }

}

