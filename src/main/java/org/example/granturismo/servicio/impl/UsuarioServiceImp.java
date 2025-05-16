package org.example.granturismo.servicio.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.granturismo.dtos.UsuarioDTO;
import org.example.granturismo.excepciones.ModelNotFoundException;
import org.example.granturismo.mappers.UsuarioMapper;
import org.example.granturismo.modelo.Rol;
import org.example.granturismo.modelo.Usuario;
import org.example.granturismo.modelo.UsuarioRol;
import org.example.granturismo.modelo.VerificationToken;
import org.example.granturismo.repositorio.ICrudGenericoRepository;
import org.example.granturismo.repositorio.IUsuarioRepository;
import org.example.granturismo.repositorio.VerificationTokenRepository;
import org.example.granturismo.servicio.IRolService;
import org.example.granturismo.servicio.IUsuarioRolService;
import org.example.granturismo.servicio.IUsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.CharBuffer;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UsuarioServiceImp extends CrudGenericoServiceImp<Usuario, Long> implements IUsuarioService {
    private final IUsuarioRepository repo;

    private final IRolService rolService;
    private final IUsuarioRolService iurService;

    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper userMapper;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailServiceImpl emailService;
    private final EmailTemplateService emailTemplateService;

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return repo.findOneByEmail(email);
    }

    @Override
    protected ICrudGenericoRepository<Usuario, Long> getRepo() {
        return repo;
    }



    @Override
    public UsuarioDTO login(UsuarioDTO.CredencialesDto credentialsDto) {
        Usuario user = repo.findOneByEmail(credentialsDto.email())
                .orElseThrow(() -> new ModelNotFoundException("Unknown user", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.clave()), user.getClave())) {
            return userMapper.toDTO(user);
        }
        if (user.getVerificado() != Usuario.EstadoVerificado.SI) {
            throw new ModelNotFoundException("Usuario no verificado. Por favor verifica tu correo.", HttpStatus.FORBIDDEN);
        }

        throw new ModelNotFoundException("Invalid password", HttpStatus.BAD_REQUEST);
    }


    @Override
    public UsuarioDTO registerUser(UsuarioDTO.UsuarioCrearDto userDto) {
        log.info("Intentando registrar nuevo usuario: {}", userDto.email());

        Optional<Usuario> optionalUser = repo.findOneByEmail(userDto.email());
        if (optionalUser.isPresent()) {
            log.warn("Nombre de usuario ya existente: {}", userDto.email());
            throw new ModelNotFoundException("El nombre de usuario ya está en uso", HttpStatus.BAD_REQUEST);
        }

        // Mapear DTO a entidad
        Usuario user = userMapper.toEntityFromCADTO(userDto);
        user.setClave(passwordEncoder.encode(CharBuffer.wrap(userDto.clave())));
        user.setVerificado(Usuario.EstadoVerificado.NO);

        Usuario savedUser = repo.saveAndFlush(user); // 👉 guardar y sincronizar con la base de datos

        // Asignar rol USER
        Rol rol = rolService.getByNombre(Rol.RolNombre.USER)
                .orElseThrow(() -> new ModelNotFoundException("Rol USER no encontrado", HttpStatus.NOT_FOUND));

        iurService.save(UsuarioRol.builder()
                .usuario(savedUser)
                .rol(rol)
                .build());

        log.info("Usuario registrado exitosamente con rol USER: {}", savedUser.getEmail());

        // 🛡️ Verificar si ya existe un token para este usuario
        Optional<VerificationToken> existingTokenOpt = verificationTokenRepository.findByUsuario(savedUser);
        VerificationToken vt;

        if (existingTokenOpt.isPresent()) {
            // Si ya existe, actualizar el token y la fecha
            vt = existingTokenOpt.get();
            vt.setToken(UUID.randomUUID().toString());
            vt.setFechaExpiracion(LocalDateTime.now().plusHours(24));
        } else {
            // Si no existe, crear uno nuevo
            vt = new VerificationToken();
            vt.setToken(UUID.randomUUID().toString());
            vt.setUsuario(savedUser);
            vt.setFechaExpiracion(LocalDateTime.now().plusHours(24));

        }
        verificationTokenRepository.save(vt);
        // 📨 Enviar email de verificación
        String subject = "Por favor verifica tu correo";
        String verificationLink = "http://localhost:8080/users/verify?token=" + vt.getToken();
        Map<String, String> variables = Map.of(
                "email", savedUser.getEmail(),
                "verificationLink", verificationLink
        );

        String body = emailTemplateService.loadTemplate("templates/email/verification-email.html", variables);
        emailService.sendVerificationEmail(savedUser.getEmail(), subject, body);

        log.info("Correo de verificación enviado a: {}", savedUser.getEmail());

        return userMapper.toDTO(savedUser);
    }


    @Override
    public UsuarioDTO registerByAdmin(UsuarioDTO.UsuarioCrearConRolDto userDto) {
        log.info("ADMIN intentando registrar usuario: {} con rol: {}", userDto.email(), userDto.rol());

        Optional<Usuario> optionalUser = repo.findOneByEmail(userDto.email());
        if (optionalUser.isPresent()) {
            log.warn("Nombre de usuario ya existente: {}", userDto.email());
            throw new ModelNotFoundException("El nombre de usuario ya está en uso", HttpStatus.BAD_REQUEST);
        }

        Usuario user = userMapper.toEntityFromAdminDTO(userDto);
        user.setClave(passwordEncoder.encode(CharBuffer.wrap(userDto.clave())));
        user.setVerificado(Usuario.EstadoVerificado.SI);
        Usuario savedUser = repo.save(user);

        Rol.RolNombre rolNombre;
        try {
            rolNombre = Rol.RolNombre.valueOf(userDto.rol().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.error("Rol inválido proporcionado: {}", userDto.rol());
            throw new ModelNotFoundException("Rol inválido", HttpStatus.BAD_REQUEST);
        }

        Rol rol = rolService.getByNombre(rolNombre)
                .orElseThrow(() -> new ModelNotFoundException("Rol no encontrado", HttpStatus.NOT_FOUND));

        iurService.save(UsuarioRol.builder()
                .usuario(savedUser)
                .rol(rol)
                .build());

        log.info("Usuario creado exitosamente por ADMIN: {} con rol: {}", savedUser.getEmail(), rol.getNombre());
        return userMapper.toDTO(savedUser);
    }



}
