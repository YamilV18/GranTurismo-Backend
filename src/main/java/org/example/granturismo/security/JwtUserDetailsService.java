package org.example.granturismo.security;

import lombok.RequiredArgsConstructor;
import org.example.granturismo.modelo.Usuario;
import org.example.granturismo.modelo.UsuarioRol;
import org.example.granturismo.repositorio.IUsuarioRepository;
import org.example.granturismo.repositorio.IUsuarioRolRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // Importar Collectors

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

    private final IUsuarioRolRepository repo;
    private final IUsuarioRepository repoU;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca el usuario por email
        Usuario u = repoU.findOneByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));

        // Busca los roles del usuario
        List<UsuarioRol> userRoles = repo.findByUsuario(u); // Asumiendo que IUsuarioRolRepository tiene un findByUsuario

        // Mapea los roles a GrantedAuthority
        List<GrantedAuthority> roles = userRoles.stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getRol().getNombre().name()))
                .collect(Collectors.toList());

        // Retorna tu CustomUserDetails con el ID del usuario
        return new CustomUserDetails(u.getIdUsuario(), u.getEmail(), u.getClave(), roles);
    }
}