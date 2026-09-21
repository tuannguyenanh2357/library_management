package com.library.service.interfaces;

public interface EmailService {
    void sendEmail(String to, String subject, String text);
}
