package org.example.granturismo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UsuarioDTO {
    private Long idUsuario;
    @NotNull
    private String email;

    //@NotNull
    //private String clave;

    public record CredencialesDto(@Email String email, char[] clave) { }

    public record UsuarioCrearDto(@Email String email, char[] clave) { }

    // Para admins (incluye rol explícito)
    public record UsuarioCrearConRolDto(@Email String email, char[] clave, String rol) {}
}
