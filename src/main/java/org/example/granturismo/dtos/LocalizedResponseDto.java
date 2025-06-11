package org.example.granturismo.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// REMOVE "extends PaqueteDTO" here
public class LocalizedResponseDto<T> {
    private T data; // This will hold your PaqueteDTO or PaqueteListDTO
    private String appliedLanguage;
    private String appliedCurrency;
    private boolean wasTranslated;
    private boolean wasCurrencyConverted;
    private Double exchangeRate;
}