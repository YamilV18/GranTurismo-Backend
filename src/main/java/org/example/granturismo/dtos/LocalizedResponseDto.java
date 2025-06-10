package org.example.granturismo.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalizedResponseDto<T> {
    private T data;
    private String appliedLanguage;
    private String appliedCurrency;
    private boolean wasTranslated;
    private boolean wasCurrencyConverted;
}