package com.example.linktosync.Tokens.models;

import java.time.LocalDateTime;

import javax.validation.constraints.Size;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import com.example.linktosync.Users.models.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Tokens {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(unique = true, nullable = false, length = 512)
  @Size(max = 512, message = "Token length must be less than 512 characters")
  private String accessToken;

  @Column(name = "refresh_token")
  private String refreshToken;

  @Enumerated(EnumType.STRING)
  public TokenType tokenType = TokenType.BEARER;

  @Column(name = "revoked",nullable = false)
  private boolean revoked;

  @Column(name = "accessTokenExpired",nullable = false)
  private boolean accessTokenExpired;

  @Column(name = "refreshTokenExpired",nullable = false)
  private boolean refreshTokenExpired;

  // @Column(name = "is_logged_out")
  // private boolean loggedOut;
  // @Column(name = "refreshTokenExpired",nullable = false)
  // private boolean refreshTokenExpired;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

 


}

