package org.example.granturismo.dtos;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.granturismo.modelo.Usuario;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PreferenciasDTO {

    private Long idPreferencia;
    private String moneda;
    private String idioma;
    private UsuarioDTO usuario;


    public record PreferenciasCADTO(
            Long idPreferencia,
            String moneda,
            String idioma,
            Long usuario
    ){}
}

