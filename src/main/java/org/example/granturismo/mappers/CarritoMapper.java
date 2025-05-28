package org.example.granturismo.mappers;

import org.example.granturismo.dtos.CarritoDTO;
import org.example.granturismo.dtos.ReservaDTO;
import org.example.granturismo.modelo.Carrito;
import org.example.granturismo.modelo.Reserva;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarritoMapper extends GenericMapper<CarritoDTO, Carrito>{

    @Mapping(target = "usuario", ignore = true)

    Carrito toEntityFromCADTO(CarritoDTO.CarritoCADTO carritoCrearDTO);
}


