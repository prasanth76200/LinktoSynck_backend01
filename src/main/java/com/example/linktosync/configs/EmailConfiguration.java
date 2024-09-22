package com.example.linktosync.configs;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import io.github.cdimascio.dotenv.Dotenv;


@Configuration
public class EmailConfiguration {
    
    private String emailUsername;
    private String emailPassword;

    public EmailConfiguration() {
        Dotenv dotenv = Dotenv.configure()
                .directory("/home/prasanth/Downloads/linktosync")
                .load();
        this.emailUsername = dotenv.get("EMAIL_CONFIG_NAME");
        this.emailPassword = (dotenv.get("EMAIL_CONFIG_PASSWORD"));
      
    }
     @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(emailUsername);
        mailSender.setPassword(emailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
