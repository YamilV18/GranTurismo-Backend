package org.example.granturismo.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslatedContentDto {
    private String originalText;
    private String translatedText;
    private String sourceLanguage;
    private String targetLanguage;
    private Double originalPrice;
    private Double convertedPrice;
    private String originalCurrency;
    private String targetCurrency;
    private Double exchangeRate;
}