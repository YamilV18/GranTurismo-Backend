package org.example.granturismo.repositorio;

import org.example.granturismo.modelo.Carrito;

import java.util.Optional;

public interface ICarritoRepository extends ICrudGenericoRepository<Carrito, Long> {
    Optional<Carrito> findByUsuario_IdUsuario(Long usuarioIdUsuario);
}
