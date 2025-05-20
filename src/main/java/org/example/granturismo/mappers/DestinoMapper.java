package org.example.granturismo.mappers;

import org.example.granturismo.dtos.ActividadDTO;
import org.example.granturismo.dtos.DestinoDTO;
import org.example.granturismo.dtos.ProveedorDTO;
import org.example.granturismo.modelo.Actividad;
import org.example.granturismo.modelo.Destino;
import org.example.granturismo.modelo.Proveedor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DestinoMapper extends GenericMapper<DestinoDTO, Destino> {

    @Override
    DestinoDTO toDTO(Destino entity);

    // Asegurarse de que imagenPublicId también se mapee en toEntity
    @Override
    Destino toEntity(DestinoDTO dto);

    Destino toEntityFromCADTO(DestinoDTO.DestinoCADTO destinoCrearDTO);
}
