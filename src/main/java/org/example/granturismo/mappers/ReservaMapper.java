package org.example.granturismo.mappers;

import org.example.granturismo.dtos.ReservaDTO;
import org.example.granturismo.modelo.Reserva;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservaMapper extends GenericMapper<ReservaDTO, Reserva> {

    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "paquete", ignore = true)

    Reserva toEntityFromCADTO(ReservaDTO.ReservaCADTO reservaCrearDTO);
}
