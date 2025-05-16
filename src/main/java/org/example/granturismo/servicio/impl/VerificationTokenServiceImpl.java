package org.example.granturismo.servicio.impl;

import org.example.granturismo.excepciones.ModelNotFoundException;
import org.example.granturismo.modelo.Usuario;
import org.example.granturismo.modelo.VerificationToken;
import org.example.granturismo.repositorio.IUsuarioRepository;
import org.example.granturismo.repositorio.VerificationTokenRepository;
import org.example.granturismo.servicio.IVerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationTokenServiceImpl implements IVerificationTokenService {

    private static final int EXPIRATION_MINUTES = 60 * 24; // 24 horas

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Override
    public String createVerificationToken(Usuario usuario) {
        tokenRepository.findByUsuario(usuario).ifPresent(tokenRepository::delete);

        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                .build();

        tokenRepository.save(verificationToken);
        return token;
    }

    @Override
    public boolean validateVerificationToken(String token) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) return false;

        VerificationToken verificationToken = optionalToken.get();

        VerificationToken vt = optionalToken.get();
        if (vt.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return false; // token expirado
        }

        Usuario user = vt.getUsuario();
        user.setVerificado(Usuario.EstadoVerificado.SI);
        usuarioRepository.save(user);

        verificationTokenRepository.delete(vt); // opcional: eliminar token usado
        return true;
    }

}
