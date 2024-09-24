package com.example.linktosync.Tokens.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RefreshTokenResponse {
    

  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("refresh_token")
  private String refreshToken;
  @JsonProperty("accessTokenExpires_in")
  private long accessTokenExpiresIn;
  @JsonProperty("refreshTokenExpiresIn_in")
  private long refreshTokenExpiresIn;


}
