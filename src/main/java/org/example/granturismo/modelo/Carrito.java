package org.example.granturismo.modelo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "carrito")
public class Carrito {

    public enum Estado { PENDIENTE, CONFIRMADO, CANCELADO}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito")
    private Long idCarrito ;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion ;

    @Column(name = "estado", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Carrito.Estado estado;

    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario",
            nullable = false, foreignKey = @ForeignKey(name = "FK_CARRITO_USUARIO"))
    private Usuario usuario;

}
