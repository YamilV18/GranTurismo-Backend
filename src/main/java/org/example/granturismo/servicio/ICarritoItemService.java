package org.example.granturismo.servicio;

import org.example.granturismo.dtos.CarritoDTO;
import org.example.granturismo.dtos.CarritoItemDTO;
import org.example.granturismo.modelo.Carrito;
import org.example.granturismo.modelo.CarritoItem;
import org.example.granturismo.modelo.Favorito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ICarritoItemService extends ICrudGenericoService<CarritoItem, Long> {

    CarritoItemDTO saveD(CarritoItemDTO.CarritoItemCADTO dto);

    CarritoItemDTO updateD(CarritoItemDTO.CarritoItemCADTO dto, Long id);

    Optional<CarritoItem> findByCarritoIdAndReferenciaIdAndTipo(Long carritoId, String tipo, Long referenciaId);

    List<CarritoItem> findByCarrito(Long carritoId);

    Page<CarritoItem> listaPage(Pageable pageable);
}
