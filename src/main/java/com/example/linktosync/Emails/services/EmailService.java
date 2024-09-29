package com.example.linktosync.Emails.services;

import jakarta.mail.MessagingException;


public interface EmailService {
    void sendVerificationEmail(String to, String subject, String text) throws MessagingException;
    public String createEmailContent(String verificationCode, String header, String userName);
    public String createForgotPasswordEmail(String userName, String resetLink);
}