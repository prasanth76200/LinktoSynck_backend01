

package com.example.linktosync.Emails.services.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.linktosync.Emails.services.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImp implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Override
    public void sendVerificationEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        ClassPathResource logoResource = new ClassPathResource("static/images/logo.png");
        helper.addInline("logo", logoResource);

        emailSender.send(message);
    }


    @Override
    public String createEmailContent(String content, String header,String userName) {
            return "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">"
                    + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                    + "<title>Verify With LinktoSync</title>"
                    + "</head>"
                    + "<body style=\"font-family: Helvetica, Arial, sans-serif; margin: 0; padding: 0;\">"
                    + "<div style=\"background-color: #ffffff; padding: 20px; text-align: center;\">"
                    + "<div style=\"max-width: 500px; margin: auto; background-color: #000000; padding: 20px; border-radius: 12px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                    + "<img src=\"cid:logo\" alt=\"Company Logo\" style=\"width: 150px;\">"
                    + "<h2 style=\"color: #03c9d7; font-size: 20px;\">Hey, <span style=\"color: #ffffff;\">" + userName + "</span> "  + header + "</h2>"
                    + "<p style=\"font-size: 20px; color: #e7e7e7;\">Here's your verification code to complete the sign-up process.</p>"
                    + "<p style=\"font-size: 16px; color: #9e9e9e;\">This code will expire in 1 minute.</p>"
                    + "<h3 style=\"color:#fffdfd;\">Your Verification Code:</h3>"
                    + "<span style=\"padding: 12px 24px; border-radius: 4px; color: #ffffff; background: #F15A29; display: inline-block; text-align: center; font-weight: bold; font-size: 29px;\">"
                    + content + "</span>"
                    + "<p style=\"font-size: 14px; color: #9e9e9e; margin-top: 20px;\">If you didn&#39;t request this, you can safely ignore this email.</p>"
                    + "<p style=\"font-size: 14px; color: #9e9e9e;\">Thank you for being a part of <a href=\"https://linktosync.com\" style=\"color: #F15A29;\"><b>LinktoSync</b></a>.</p>"
                    + "<p style=\"color: #03c9d7; text-decoration:none; font-size: 13px;\"><b>Copyrights &copy; 2025 LinktoSync</b></p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
        
        
    }

    @Override
    public String createForgotPasswordEmail(String userName, String resetLink) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"
                + "<title>Reset Password With LinktoSync</title>"
                + "</head>"
                + "<body style=\"font-family: Helvetica, Arial, sans-serif; margin: 0; padding: 0;\">"
                + "<div style=\"background-color: #ffffff; padding: 20px; text-align: center;\">"
                + "<div style=\"max-width: 500px; margin: auto; background-color: #000000; padding: 20px; border-radius: 12px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); \">"
                + "<img src=\"cid:logo\" alt=\"Company Logo\" style=\"width: 150px; \"/>"
                + "<h2 style=\"color: #03c9d7;\">Forgot your password?</h2>"
                + "<p style=\"font-size: 20px; color: #e7e7e7;\">Hey " + userName + ", we received a request to reset your password.</p>"
                + "<p style=\"font-size: 16px; color: #9e9e9e;\">This link will expire in 24 hours.</p>"
                + "<a href=\"" + resetLink + "\" style=\"padding: 12px 24px; border-radius: 4px; color: #ffffff; background: #f15a29; display: inline-block; text-align: center; font-weight: bold; font-size: 18px; text-decoration:none;\">"
                + "RESET MY PASSWORD"
                + "</a>"
                + "<p style=\"font-size: 14px; color: #cfcfcf; margin-top: 20px; text-align: center;\">For your security, this password reset link will expire in 24 hours. If you did not request a password reset, please ignore this email, and your password will remain unchanged.</p>"
                + "<p style=\"font-size: 14px; color: #ffffff;\">Please feel free to contact us at linktosyncofficial@gmail.com</p>"
                + "<a href=\"https://linktosync.com\" style=\"color: #03c9d7; text-decoration:none;font-size: 13px; \"><b>Copyrights &copy; 2025 LinktoSync</b></a>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }
    


}
