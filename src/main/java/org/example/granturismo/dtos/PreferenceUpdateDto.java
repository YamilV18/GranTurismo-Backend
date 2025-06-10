package org.example.granturismo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceUpdateDto {

    @NotBlank(message = "El código de moneda es requerido")
    @Pattern(regexp = "^[A-Z]{3}$", message = "El código de moneda debe tener 3 letras mayúsculas (ej: USD, EUR)")
    private String preferredCurrencyCode;

    @NotBlank(message = "El código de idioma es requerido")
    @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "El código de idioma debe seguir el formato ISO (ej: es, en, es-ES)")
    private String preferredLanguageCode;
}