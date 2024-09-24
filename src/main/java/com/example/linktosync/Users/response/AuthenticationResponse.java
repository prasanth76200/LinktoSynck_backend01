package com.example.linktosync.Users.response;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {


  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("refresh_token")
  private String refreshToken;
  @JsonProperty("accessTokenExpires_in")
  private long accessTokenExpiresIn;
  @JsonProperty("refreshTokenExpiresIn_in")
  private long refreshTokenExpiresIn;

  private UUID userId;   // Add userId
  private String userName; // Add userName
  private String userEmail;
  

  public AuthenticationResponse(String accessToken, String refreshToken, String message) {
    this.accessToken = accessToken;
  
    this.refreshToken = refreshToken;
}


}