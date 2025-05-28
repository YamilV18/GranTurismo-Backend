package org.example.granturismo.dtos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.granturismo.modelo.Actividad;
import org.example.granturismo.modelo.Carrito;
import org.example.granturismo.modelo.Servicio;
import org.example.granturismo.modelo.Usuario;

import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CarritoItemDTO {
    private Long idCarritoItem;
    private String tipo;
    private Integer cantidadPersonas;
    private LocalDateTime fechaReserva ;
    private String notas ;
    private CarritoDTO carrito;
    private ServicioDTO servicio;
    private ActividadDTO actividad;


    public record CarritoItemCADTO(
            Long idCarritoItem,
            String tipo,
            Integer cantidadPersonas,
            LocalDateTime fechaReserva,
            String notas,
            Long carrito,
            Long servicio,
            Long actividad

    ){}
}



