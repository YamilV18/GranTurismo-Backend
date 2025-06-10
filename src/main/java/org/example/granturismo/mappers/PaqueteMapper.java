package org.example.granturismo.mappers;

import org.example.granturismo.dtos.PaqueteDTO;
import org.example.granturismo.modelo.Paquete;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaqueteMapper extends GenericMapper<PaqueteDTO, Paquete> {

    // Mapeo completo para DTO principal
    @Override
    @Mapping(target = "monedaAplicada", ignore = true)
    @Mapping(target = "idiomaAplicado", ignore = true)
    @Mapping(target = "fueTraducido", ignore = true)
    @Mapping(target = "fueConvertido", ignore = true)
    @Mapping(target = "tasaCambio", ignore = true)
    // Asegurarse de que imagenPublicId también se mapee en toDTO
    PaqueteDTO toDTO(Paquete entity);

    @Override
    @Mapping(target = "detalles", ignore = true)
    @Mapping(target = "actividadDetalles", ignore = true)
        // Asegurarse de que imagenPublicId también se mapee en toEntity
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
    @Mapping(target = "monedaAplicada", ignore = true)
    @Mapping(target = "idiomaAplicado", ignore = true)
    @Mapping(target = "fueTraducido", ignore = true)
    @Mapping(target = "fueConvertido", ignore = true)
    @Mapping(target = "tasaCambio", ignore = true)
    PaqueteDTO.PaqueteListDTO toListDTO(Paquete entity);

    // Mapeo de lista de entidades a DTOs de lista
    List<PaqueteDTO.PaqueteListDTO> toListDTOs(List<Paquete> entities);

    // Método de utilidad para crear DTO localizado
    default PaqueteDTO createLocalizedDTO(Paquete entity, String monedaAplicada, String idiomaAplicado,
                                          Boolean fueTraducido, Boolean fueConvertido, Double tasaCambio) {
        PaqueteDTO dto = toDTO(entity);
        dto.setMonedaAplicada(monedaAplicada);
        dto.setIdiomaAplicado(idiomaAplicado);
        dto.setFueTraducido(fueTraducido);
        dto.setFueConvertido(fueConvertido);
        dto.setTasaCambio(tasaCambio);
        return dto;
    }

    // Método de utilidad para crear DTO de lista localizado
    default PaqueteDTO.PaqueteListDTO createLocalizedListDTO(Paquete entity, String monedaAplicada, String idiomaAplicado,
                                                             Boolean fueTraducido, Boolean fueConvertido, Double tasaCambio) {
        PaqueteDTO.PaqueteListDTO dto = toListDTO(entity);
        dto.setMonedaAplicada(monedaAplicada);
        dto.setIdiomaAplicado(idiomaAplicado);
        dto.setFueTraducido(fueTraducido);
        dto.setFueConvertido(fueConvertido);
        dto.setTasaCambio(tasaCambio);
        return dto;
    }

}
