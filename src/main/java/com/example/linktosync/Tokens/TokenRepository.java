package com.example.linktosync.Tokens;



import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Tokens, Integer> {

    @Query("""
      select t from Tokens t 
      where t.user.id = :id and (t.expired = false or t.revoked = false)
      """)
    List<Tokens> findAllValidTokenByUser(UUID id);

    Optional<Tokens> findByAccessToken(String token);

    Optional<Tokens> findByRefreshToken(String token);


    List<Tokens> findAllByUser_UserId(UUID userId);
    
    // Fetch valid tokens for a user using their user ID
    List<Tokens> findAllValidTokenByUser_UserId(UUID userId);

    // List<Tokens> findAllByUser(User user);
    
    // List<Tokens> findAllValidTokenByUser(Long userId);

    // findByRefreshToken
    //  @Override
    // void delete(@SuppressWarnings("null") Tokens token);

    // Optional<Tokens> findByToken(String token);

   
}
