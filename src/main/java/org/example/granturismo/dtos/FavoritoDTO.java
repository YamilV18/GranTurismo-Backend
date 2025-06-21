package org.example.granturismo.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FavoritoDTO {
    private Long idFavorito;
    private Long referenciaId;
    private String tipo;
    private UsuarioDTO usuario;

    public record FavoritoCADTO(
            Long idFavorito,
            Long referenciaId,
            String tipo,
            Long usuario
    ){}
}
