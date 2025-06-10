package org.example.granturismo.repositorio;

import org.example.granturismo.modelo.Preference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreferenceRepository extends JpaRepository<Preference, Long> {

    /**
     * Busca las preferencias por ID de usuario
     */
    Optional<Preference> findByIdUsuario(Long idUsuario);

    /**
     * Verifica si existen preferencias para un usuario
     */
    boolean existsByIdUsuario(Long idUsuario);

    /**
     * Elimina las preferencias de un usuario
     */
    void deleteByIdUsuario(Long idUsuario);

    /**
     * Obtiene las preferencias con información del usuario
     */
    @Query("SELECT p FROM Preference p LEFT JOIN FETCH p.usuario WHERE p.idUsuario = :idUsuario")
    Optional<Preference> findByIdUsuarioWithUser(@Param("idUsuario") Long idUsuario);
}