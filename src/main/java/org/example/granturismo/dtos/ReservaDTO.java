package org.example.granturismo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.granturismo.modelo.Reserva;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReservaDTO {

    private Long idReserva;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Reserva.Estado estado;
    private Integer cantidadPersonas;
    private String observaciones;
    private UsuarioDTO usuario;
    private PaqueteDTO paquete;



    public record ReservaCADTO(
            Long idReserva,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Reserva.Estado estado,
            Integer cantidadPersonas,
            String observaciones,
            Long usuario,
            Long paquete
    ){}
}
