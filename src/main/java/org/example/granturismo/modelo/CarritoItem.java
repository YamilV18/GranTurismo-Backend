package org.example.granturismo.modelo;

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
@Table(name = "carrito_item")
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito_item")
    private Long idCarritoItem ;

    @Column(name = "tipo", nullable = false )
    private String tipo ;

    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;

    @Column(name = "cantidad_personas")
    private Integer cantidadPersonas ;

    @Column(name = "fecha_reserva")
    private LocalDateTime fechaReserva ;

    @Column(name = "notas")
    private String notas ;

    @ManyToOne
    @JoinColumn(name = "id_carrito", referencedColumnName = "id_carrito",
            nullable = false, foreignKey = @ForeignKey(name = "FK_CARRITOITEM_CARRITO"))
    private Carrito carrito;

}