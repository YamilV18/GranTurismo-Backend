// src/main/java/org/example/granturismo/security/CustomUserDetails.java
package org.example.granturismo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {

    private final Long idUsuario; // Campo para almacenar el ID del usuario

    public CustomUserDetails(Long idUsuario, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.idUsuario = idUsuario;
    }

    // Getter para acceder al ID del usuario
    public Long getIdUsuario() {
        return idUsuario;
    }

    // Puedes agregar otros getters si necesitas más información del usuario en el UserDetails
}