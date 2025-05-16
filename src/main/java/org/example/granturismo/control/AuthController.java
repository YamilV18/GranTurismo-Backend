package org.example.granturismo.control;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.UsuarioDTO;
import org.example.granturismo.dtos.UsuarioLoginRespuestaDTO;
import org.example.granturismo.excepciones.ModelNotFoundException;
import org.example.granturismo.modelo.Usuario;
import org.example.granturismo.modelo.VerificationToken;
import org.example.granturismo.repositorio.IUsuarioRepository;
import org.example.granturismo.repositorio.VerificationTokenRepository;
import org.example.granturismo.security.JwtTokenUtil;
import org.example.granturismo.security.JwtUserDetailsService;
import org.example.granturismo.security.PermitRoles;
import org.example.granturismo.servicio.IUsuarioService;
import org.example.granturismo.servicio.impl.EmailServiceImpl;
import org.example.granturismo.servicio.impl.VerificationTokenServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class AuthController {
    private final IUsuarioService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService jwtUserDetailsService;
    private final VerificationTokenServiceImpl tokenService;
    private final IUsuarioRepository usuarioRepository;
    private final EmailServiceImpl emailService;
    private final VerificationTokenRepository verificationTokenRepository;

    @PostMapping("/login")
    public ResponseEntity<UsuarioLoginRespuestaDTO> login(@RequestBody @Valid UsuarioDTO.CredencialesDto credentialsDto, HttpServletRequest request) {
        UsuarioDTO userDto = userService.login(credentialsDto);
        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(credentialsDto.email());
        String token = jwtTokenUtil.generateToken(userDetails);
        request.getSession().setAttribute("USER_SESSION", userDto.getEmail());

        UsuarioLoginRespuestaDTO respuesta = new UsuarioLoginRespuestaDTO(userDto.getIdUsuario(), userDto.getEmail(), token);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UsuarioDTO.UsuarioCrearDto userDto) {
        userService.registerUser(userDto);  // Ya realiza todo lo necesario
        return ResponseEntity.ok("Usuario creado exitosamente. Por favor revisa tu correo para verificar la cuenta.");
    }


    @PermitRoles("ADMIN")
    @PostMapping("/create")
    public ResponseEntity<UsuarioDTO> createUserByAdmin(@RequestBody @Valid UsuarioDTO.UsuarioCrearConRolDto user) {
        UsuarioDTO createdUser = userService.registerByAdmin(user);
        return ResponseEntity.created(URI.create("/users/" + createdUser.getEmail())).body(createdUser);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam String token) {
        boolean valid = tokenService.validateVerificationToken(token);
        if (valid) {
            return ResponseEntity.ok("Tu cuenta ha sido verificada correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El token es inválido o ha expirado.");
        }
    }


}

