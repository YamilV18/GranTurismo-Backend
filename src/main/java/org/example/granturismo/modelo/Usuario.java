package org.example.granturismo.modelo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    public enum EstadoVerificado {
        SI, NO
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "clave", nullable = false, length = 100)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(name = "verificado", nullable = false, length = 2)
    private EstadoVerificado verificado;
}
