

package com.example.linktosync.Users.services;

import com.example.linktosync.Users.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {

    UserDetails loadUserByUsername(String userName);

    UserDto getUserByUsername(String userName);
}
