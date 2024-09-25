package com.example.linktosync.Tokens.services.imp;

import com.example.linktosync.Tokens.models.Tokens;
import com.example.linktosync.Tokens.repository.TokenRepository;
import com.example.linktosync.Tokens.services.TokenService;
import com.example.linktosync.Users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class TokenServiceImp implements TokenService {

    @Autowired
    private TokenRepository tokenRepository;

//    public Optional<Tokens> getTokenByUserName(String userName) {
//        return tokenRepository.findByUser_UserName(userName);
//    }
}
