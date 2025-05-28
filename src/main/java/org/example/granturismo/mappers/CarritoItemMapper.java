package org.example.granturismo.mappers;

import org.example.granturismo.dtos.CarritoDTO;
import org.example.granturismo.dtos.CarritoItemDTO;
import org.example.granturismo.modelo.Carrito;
import org.example.granturismo.modelo.CarritoItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarritoItemMapper extends GenericMapper<CarritoItemDTO, CarritoItem> {

    @Mapping(target = "usuario", ignore = true)

    CarritoItem toEntityFromCADTO(CarritoItemDTO.CarritoItemCADTO carritoitemCrearDTO);
}
