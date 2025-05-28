package org.example.granturismo.servicio;

import org.example.granturismo.dtos.CarritoDTO;
import org.example.granturismo.dtos.CarritoItemDTO;
import org.example.granturismo.modelo.Carrito;
import org.example.granturismo.modelo.CarritoItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICarritoItemService extends ICrudGenericoService<CarritoItem, Long> {

    CarritoItemDTO saveD(CarritoItemDTO.CarritoItemCADTO dto);

    CarritoItemDTO updateD(CarritoItemDTO.CarritoItemCADTO dto, Long id);

    Page<CarritoItem> listaPage(Pageable pageable);
}
