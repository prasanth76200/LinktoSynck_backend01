package com.example.linktosync.Users.dto;

import com.example.linktosync.Users.models.Role;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;



@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class ResetPasswordDto {

    @NotBlank(message = "Email address is required.")
    @Email(message = "Please provide a valid email address.")
    private String userEmail;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    @Pattern(regexp = "(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}",
             message = "Password must include at least one uppercase letter, one lowercase letter, one digit, and one special character.")
    private String userPassword;

    @NotBlank(message = "New Password is required.")
    @Size(min = 8, message = "New Password must be at least 8 characters long.")
    @Pattern(regexp = "(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}",
             message = "Password must include at least one uppercase letter, one lowercase letter, one digit, and one special character.")
    private String newUserPassword;
     
 @NotBlank(message = "Role is mandatory")
    @NotNull(message = "Role cannot be null")
   @Enumerated(value = EnumType.STRING)
    private Role role;



    // public String getUserEmail() {
    //     return userEmail;
    // }
    
    // public void setUserEmail(String userEmail) {
    //     this.userEmail = userEmail;
    // }
    
    // public String getUserPassword() {
    //     return userPassword;
    // }
    
    // public void setUserPassword(String userPassword) {
    //     this.userPassword = userPassword;
    // }
    
    // public String getNewUserPassword() {
    //     return newUserPassword;
    // }
    
    // public void setNewUserPassword(String newUserPassword) {
    //     this.newUserPassword = newUserPassword;
    // }
    
}
