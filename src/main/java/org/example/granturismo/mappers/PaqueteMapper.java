package org.example.granturismo.mappers;

import org.example.granturismo.dtos.PaqueteDTO;
import org.example.granturismo.modelo.Paquete;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.InheritInverseConfiguration; // No longer strictly needed for this file, but fine to keep if used elsewhere

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaqueteMapper extends GenericMapper<PaqueteDTO, Paquete> {

    // Mapeo completo para DTO principal
    @Override
    // REMOVE all these ignore mappings for localization metadata
    // @Mapping(target = "monedaAplicada", ignore = true)
    // @Mapping(target = "idiomaAplicado", ignore = true)
    // @Mapping(target = "fueTraducido", ignore = true)
    // @Mapping(target = "fueConvertido", ignore = true)
    // @Mapping(target = "tasaCambio", ignore = true)
    PaqueteDTO toDTO(Paquete entity);

    @Override
    @Mapping(target = "detalles", ignore = true)
    @Mapping(target = "actividadDetalles", ignore = true)
    Paquete toEntity(PaqueteDTO dto);

    // Mapeo desde DTO de creación/actualización
    @Mapping(target = "destino", ignore = true)
    @Mapping(target = "proveedor", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    @Mapping(target = "actividadDetalles", ignore = true)
    @Mapping(target = "imagenUrl", ignore = true)
    @Mapping(target = "imagenPublicId", ignore = true)
    @Mapping(target = "monedaOriginal", source = "monedaOriginal", defaultValue = "PEN")
    @Mapping(target = "idiomaOriginal", source = "idiomaOriginal", defaultValue = "es")
    Paquete toEntityFromCADTO(PaqueteDTO.PaqueteCADTO paqueteCrearDTO);

    // Mapeo para DTO de lista
    @Mapping(target = "proveedorNombre", source = "proveedor.nombreCompleto")
    @Mapping(target = "destinoNombre", source = "destino.nombre")
    // REMOVE all these ignore mappings for localization metadata
    // @Mapping(target = "monedaAplicada", ignore = true)
    // @Mapping(target = "idiomaAplicado", ignore = true)
    // @Mapping(target = "fueTraducido", ignore = true)
    // @Mapping(target = "fueConvertido", ignore = true)
    // @Mapping(target = "tasaCambio", ignore = true)
    PaqueteDTO.PaqueteListDTO toListDTO(Paquete entity);

    // Mapeo de lista de entidades a DTOs de lista
    List<PaqueteDTO.PaqueteListDTO> toListDTOs(List<Paquete> entities);

    // REMOVE these utility methods, their logic is now handled by LocalizedResponseDto and LocalizationService
    // default PaqueteDTO createLocalizedDTO(...) { ... }
    // default PaqueteDTO.PaqueteListDTO createLocalizedListDTO(...) { ... }
}