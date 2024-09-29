package com.example.linktosync.Users.response;
import java.util.UUID;

import com.example.linktosync.Users.models.Role;
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

  private UUID userId;  
  private String userName; 
  private String userEmail;
  private Role role;
  

  public AuthenticationResponse(String accessToken, String refreshToken) {
    this.accessToken = accessToken;
  
    this.refreshToken = refreshToken;
}


}