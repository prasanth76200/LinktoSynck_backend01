package com.example.linktosync.utils;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtils {

    // Method to delete a specific cookie
    public void deleteCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0); // Delete the cookie
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Ensure security settings are consistent
        cookie.setPath("/"); // Same path as where it was set
        response.addCookie(cookie);
    }

    // Method to delete multiple cookies (for logout)
    public void deleteCookies(HttpServletResponse response) {
        // Method to delete cookies by setting their max age to 0
        Cookie[] cookies = { new Cookie("sessionID", null), new Cookie("sessionKey", null) };
        for (Cookie cookie : cookies) {
            cookie.setMaxAge(0); // Set to 0 to delete
            cookie.setHttpOnly(true);
            cookie.setSecure(true); // Ensure security settings are consistent
            cookie.setPath("/"); // Same path as where it was set
            response.addCookie(cookie);
        }
    }
    
}
