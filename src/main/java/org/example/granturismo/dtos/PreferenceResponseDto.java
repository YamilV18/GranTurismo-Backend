package org.example.granturismo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para obtener preferencias
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceResponseDto {
    private Long idPreference;
    private Long idUsuario;
    private String preferredCurrencyCode;
    private String preferredLanguageCode;
}