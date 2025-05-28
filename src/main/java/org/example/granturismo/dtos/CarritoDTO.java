package org.example.granturismo.dtos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.granturismo.modelo.Carrito;
import org.example.granturismo.modelo.Reserva;
import org.example.granturismo.modelo.Usuario;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CarritoDTO {

    private Long idCarrito;
    private LocalDateTime fechaCreacion;
    private Carrito.Estado estado;
    private UsuarioDTO usuario;


    public record CarritoCADTO(
            Long idCarrito,
            LocalDateTime fechaCreacion,
            Carrito.Estado estado,
            Long usuario
    ){}
}