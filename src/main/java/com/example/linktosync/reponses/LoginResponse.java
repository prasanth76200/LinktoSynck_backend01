package com.example.linktosync.reponses;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

   

  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("refresh_token")
  private String refreshToken;

    private long expiresIn;

 

    // public long getExpiresIn() {
    //     return expiresIn;
    // }

    // public LoginResponse setExpiresIn(long expiresIn) {
    //     this.expiresIn = expiresIn;
    //     return this; // Return the current instance to allow method chaining
    // }
}
