package com.example.linktosync.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.linktosync.model.User;

// import com.example.linktosync.User.User;
// import com.example.linktosync.model.User;

@Repository
public interface  UserEmailRepository extends JpaRepository<User, UUID>{

    Optional<User> findByUserEmail(String userEmail);


}
