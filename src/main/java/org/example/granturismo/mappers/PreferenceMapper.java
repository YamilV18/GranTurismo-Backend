package org.example.granturismo.mappers;

import org.example.granturismo.dtos.PreferenceResponseDto;
import org.example.granturismo.dtos.PreferenceUpdateDto;
import org.example.granturismo.modelo.Preference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PreferenceMapper {

    /**
     * Convierte entidad a DTO de respuesta
     */
    PreferenceResponseDto toResponseDto(Preference preference);

    /**
     * Convierte DTO de actualización a entidad
     */
    @Mapping(target = "idPreference", ignore = true)
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    Preference toEntity(PreferenceUpdateDto updateDto);

    /**
     * Actualiza una entidad existente con datos del DTO
     */
    @Mapping(target = "idPreference", ignore = true)
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    void updateEntityFromDto(PreferenceUpdateDto updateDto, @MappingTarget Preference preference);

    /**
     * Crea una preferencia por defecto para un usuario
     */
    default Preference createDefaultPreference(Long idUsuario) {
        return Preference.builder()
                .idUsuario(idUsuario)
                .preferredCurrencyCode("PEN")
                .preferredLanguageCode("es")
                .build();
    }
}