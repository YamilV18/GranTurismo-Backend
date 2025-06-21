package org.example.granturismo.mappers;

import org.example.granturismo.dtos.FavoritoDTO;
import org.example.granturismo.modelo.Favorito;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FavoritoMapper extends GenericMapper<FavoritoDTO, Favorito> {

    @Mapping(target = "usuario", ignore = true)
    Favorito toEntityFromCADTO(FavoritoDTO.FavoritoCADTO favoritoCrearDTO);
}