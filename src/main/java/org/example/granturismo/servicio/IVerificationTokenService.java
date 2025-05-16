package org.example.granturismo.servicio;

import org.example.granturismo.modelo.Usuario;

public interface IVerificationTokenService {
    String createVerificationToken(Usuario usuario);
    boolean validateVerificationToken(String token);
}