package org.example.granturismo.repositorio;

import org.example.granturismo.modelo.Usuario;
import org.example.granturismo.modelo.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUsuario(Usuario usuario);
    void deleteByUsuario(Usuario usuario);
}
