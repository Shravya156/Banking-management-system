package com.shravya.bankingapp.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // Log the error but don't crash the app if email fails
            System.err.println("Email failed to send: " + e.getMessage());
        }
    }
    public void sendPinChangeNotification(String toEmail) {
        sendEmail(toEmail, "Security Alert: PIN Changed",
                "The transaction PIN for your account was recently changed. If this wasn't you, please lock your account immediately.");
    }
}