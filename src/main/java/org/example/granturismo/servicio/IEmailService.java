package org.example.granturismo.servicio;

public interface IEmailService {
    void sendVerificationEmail(String to, String subject, String body);
}

