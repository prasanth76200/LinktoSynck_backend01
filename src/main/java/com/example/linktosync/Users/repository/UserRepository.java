package com.example.linktosync.Users.repository;



import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.linktosync.Users.models.User;


@Repository

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUserName(String userName);

    Optional<User> findByUserEmail(String userEmail);

    User findByUserEmailIgnoreCase(String emailId);

    Boolean existsByUserEmail(String email);

//   User updateUserPassword(String password);
   
    Optional<User> findByVerificationCode(String verificationCode);
    // @Override
    // List<User> findAll();

    // public void save(User verifiedUser);


}   
