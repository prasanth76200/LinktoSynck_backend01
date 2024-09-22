package com.example.linktosync.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.linktosync.dto.UserDto;
import com.example.linktosync.model.User;
import com.example.linktosync.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    

public final UserRepository userRepository;



public List<User> getAllUsers() {
    return userRepository.findAll();
}


    public UserDto getUserById(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> new UserDto(user.getUserId(), user.getUsername(), user.getUserEmail())) // Convert entity to DTO
                .orElseThrow();
    }

}
