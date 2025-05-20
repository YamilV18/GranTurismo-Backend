package org.example.granturismo.mappers;

import org.example.granturismo.dtos.ActividadDTO;
import org.example.granturismo.dtos.PaqueteDTO;
import org.example.granturismo.modelo.Actividad;
import org.example.granturismo.modelo.Paquete;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActividadMapper extends GenericMapper<ActividadDTO, Actividad> {
    // Asegurarse de que imagenPublicId también se mapee en toDTO
    @Override
    ActividadDTO toDTO(Actividad entity);

    // Asegurarse de que imagenPublicId también se mapee en toEntity
    @Override
    Actividad toEntity(ActividadDTO dto);
    Actividad toEntityFromCADTO(ActividadDTO.ActividadCADTO actividadCrearDTO);
}
