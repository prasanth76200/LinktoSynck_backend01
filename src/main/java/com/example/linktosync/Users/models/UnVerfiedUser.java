package com.example.linktosync.Users.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "UnVerifiedUser")
@Accessors(chain = true) 
public class UnVerfiedUser {
    
     @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @NotBlank(message = "Name is mandatory")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    @Column(name = "user_name", unique = true, nullable = false, length = 255)
    private String userName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is mandatory")
    @Column(name = "user_email", unique = true, nullable = false, length = 255)
    private String userEmail;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password should be at least 8 characters long")
    @Column(name = "user_password", nullable = false, length = 255)
    private String userPassword; // Ensure this is hashed

    @Column(name = "role", nullable = false)
    // @NotNull(message = "Role cannot be null")
    @Enumerated(value = EnumType.STRING)
    private Role role;
 
    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_expiration")
    private LocalDateTime verificationCodeExpiresAt;

    @Column(name = "isVerified")
    private Boolean isVerified = false;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UnVerfiedUser(LocalDateTime createdAt, Role role, LocalDateTime updatedAt, String userEmail, UUID userId, String userName, String userPassword, String verificationCode, LocalDateTime verificationCodeExpiresAt) {
        this.createdAt = createdAt;
        this.role = role;
        this.updatedAt = updatedAt;
        this.userEmail = userEmail;
        this.userId = userId;
        this.userName = userName;
        this.userPassword = userPassword;
        this.verificationCode = verificationCode;
        this.verificationCodeExpiresAt = verificationCodeExpiresAt;
    }

  

}
