package org.example.granturismo.repositorio;

import org.example.granturismo.modelo.Carrito;
import org.example.granturismo.modelo.CarritoItem;

import java.util.List;
import java.util.Optional;

public interface ICarritoItemRepository  extends  ICrudGenericoRepository<CarritoItem, Long>{
    Optional<CarritoItem> findByCarrito_IdCarritoAndReferenciaIdAndTipo(Long carritoId, Long referenciaId, String tipo);
    List<CarritoItem> findByCarrito_IdCarrito(Long carritoId);

    Long carrito(Carrito carrito);
}
