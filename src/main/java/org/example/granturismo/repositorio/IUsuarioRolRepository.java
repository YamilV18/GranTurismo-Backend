package org.example.granturismo.repositorio;

import org.example.granturismo.modelo.Usuario;
import org.example.granturismo.modelo.UsuarioRol;
import org.example.granturismo.modelo.UsuarioRolPK;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IUsuarioRolRepository extends ICrudGenericoRepository<UsuarioRol, UsuarioRolPK>{
    @Query("SELECT ur FROM UsuarioRol ur WHERE ur.usuario.email = :email")//Consulta JPQL
    List<UsuarioRol> findOneByUsuarioUser(@Param("email") String email);

    List<UsuarioRol> findByUsuario(Usuario usuario);

    List<UsuarioRol> findByUsuario(Usuario usuario, Sort sort, Limit limit);
}