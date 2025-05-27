package org.example.granturismo.modelo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity

@Table(name = "reservas")
public class Reserva {

    public enum Estado { PENDIENTE, CONFIRMADA, CANCELADA, FINALIZADA, NO_ASISTIO}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long idReserva;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(name = "estado", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Reserva.Estado estado;

    @Column(name = "cantidad_personas", nullable = false)
    private Integer cantidadPersonas;

    @Column(name = "observaciones", nullable = false, columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario",
            nullable = false, foreignKey = @ForeignKey(name = "FK_USUARIO_RESERVA"))
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_paquete", referencedColumnName = "id_paquete",
            nullable = false, foreignKey = @ForeignKey(name = "FK_PAQUETE_RESERVA"))
    private Paquete paquete;
}
