package com.example.linktosync.Users.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.linktosync.Users.models.UnVerfiedUser;

@Repository
public interface  UnVerfiedUserRepository extends JpaRepository<UnVerfiedUser, UUID> {
 Optional<UnVerfiedUser> findByUserName(String userName);

    Optional<UnVerfiedUser> findByUserEmail(String userEmail);
   


    
}
