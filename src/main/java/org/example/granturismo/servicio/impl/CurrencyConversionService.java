package org.example.granturismo.servicio.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.granturismo.dtos.TranslatedContentDto;
import org.example.granturismo.excepciones.ExcepcionPersonalizada;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@Slf4j
public class CurrencyConversionService {

    @Value("${exchangerate.api.key:}")
    private String exchangeRateApiKey;

    @Value("${exchangerate.api.url:https://v6.exchangerate-api.com/v6}")
    private String exchangeRateApiUrl;

    private final RestTemplate restTemplate;

    public CurrencyConversionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Convierte un precio de una moneda a otra
     */
    public TranslatedContentDto convertCurrency(Double amount, String fromCurrency, String toCurrency) {
        log.info("Iniciando conversión de moneda. Monto: {}, De: {}, A: {}", amount, fromCurrency, toCurrency);

        if (amount == null || amount <= 0) {
            log.warn("Monto inválido para conversión: {}", amount);
            return TranslatedContentDto.builder()
                    .originalPrice(amount)
                    .convertedPrice(amount)
                    .originalCurrency(fromCurrency)
                    .targetCurrency(toCurrency)
                    .exchangeRate(1.0)
                    .build();
        }

        if (!StringUtils.hasText(fromCurrency) || !StringUtils.hasText(toCurrency)) {
            log.warn("Códigos de moneda inválidos. From: {}, To: {}", fromCurrency, toCurrency);
            return TranslatedContentDto.builder()
                    .originalPrice(amount)
                    .convertedPrice(amount)
                    .originalCurrency(fromCurrency)
                    .targetCurrency(toCurrency)
                    .exchangeRate(1.0)
                    .build();
        }

        // Si las monedas son iguales, no convertir
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            log.info("Monedas origen y destino son iguales. No se requiere conversión.");
            return TranslatedContentDto.builder()
                    .originalPrice(amount)
                    .convertedPrice(amount)
                    .originalCurrency(fromCurrency)
                    .targetCurrency(toCurrency)
                    .exchangeRate(1.0)
                    .build();
        }

        if (!StringUtils.hasText(exchangeRateApiKey)) {
            log.warn("API Key de ExchangeRate-API no configurada. Retornando precio original.");
            return TranslatedContentDto.builder()
                    .originalPrice(amount)
                    .convertedPrice(amount)
                    .originalCurrency(fromCurrency)
                    .targetCurrency(toCurrency)
                    .exchangeRate(1.0)
                    .build();
        }

        try {
            // Obtener tasa de cambio
            Double exchangeRate = getExchangeRate(fromCurrency, toCurrency);

            if (exchangeRate == null || exchangeRate <= 0) {
                log.warn("No se pudo obtener tasa de cambio válida");
                throw new ExcepcionPersonalizada("No se pudo obtener la tasa de cambio");
            }

            // Calcular precio convertido
            BigDecimal originalAmount = BigDecimal.valueOf(amount);
            BigDecimal rate = BigDecimal.valueOf(exchangeRate);
            BigDecimal convertedAmount = originalAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);

            log.info("Conversión completada. Tasa: {}, Monto convertido: {}", exchangeRate, convertedAmount);

            return TranslatedContentDto.builder()
                    .originalPrice(amount)
                    .convertedPrice(convertedAmount.doubleValue())
                    .originalCurrency(fromCurrency.toUpperCase())
                    .targetCurrency(toCurrency.toUpperCase())
                    .exchangeRate(exchangeRate)
                    .build();

        } catch (Exception e) {
            log.error("Error al convertir moneda: {}", e.getMessage(), e);

            // En caso de error, retornar el precio original
            return TranslatedContentDto.builder()
                    .originalPrice(amount)
                    .convertedPrice(amount)
                    .originalCurrency(fromCurrency)
                    .targetCurrency(toCurrency)
                    .exchangeRate(1.0)
                    .build();
        }
    }

    /**
     * Obtiene la tasa de cambio entre dos monedas
     */
    private Double getExchangeRate(String fromCurrency, String toCurrency) {
        try {
            String url = String.format("%s/%s/pair/%s/%s",
                    exchangeRateApiUrl, exchangeRateApiKey,
                    fromCurrency.toUpperCase(), toCurrency.toUpperCase());

            log.debug("Consultando tasa de cambio en: {}", url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String result = (String) responseBody.get("result");

                if ("success".equals(result)) {
                    Object conversionRateObj = responseBody.get("conversion_rate");

                    if (conversionRateObj instanceof Number) {
                        return ((Number) conversionRateObj).doubleValue();
                    }
                } else {
                    String errorType = (String) responseBody.get("error-type");
                    log.warn("Error en API de ExchangeRate: {}", errorType);
                }
            }

            return null;

        } catch (Exception e) {
            log.error("Error al obtener tasa de cambio: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Obtiene múltiples tasas de cambio para una moneda base
     */
    public Map<String, Double> getExchangeRates(String baseCurrency) {
        try {
            String url = String.format("%s/%s/latest/%s",
                    exchangeRateApiUrl, exchangeRateApiKey, baseCurrency.toUpperCase());

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String result = (String) responseBody.get("result");

                if ("success".equals(result)) {
                    return (Map<String, Double>) responseBody.get("conversion_rates");
                }
            }

            return null;

        } catch (Exception e) {
            log.error("Error al obtener tasas de cambio: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Verifica si el servicio de conversión está disponible
     */
    public boolean isCurrencyConversionServiceAvailable() {
        return StringUtils.hasText(exchangeRateApiKey);
    }

    /**
     * Valida si un código de moneda es válido
     */
    public boolean isValidCurrencyCode(String currencyCode) {
        if (!StringUtils.hasText(currencyCode) || currencyCode.length() != 3) {
            return false;
        }

        // Lista de códigos de moneda comunes
        String[] validCurrencies = {
                "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "SEK", "NZD",
                "MXN", "SGD", "HKD", "NOK", "KRW", "TRY", "RUB", "INR", "BRL", "ZAR",
                "PLN", "THB", "IDR", "HUF", "CZK", "ILS", "CLP", "PHP", "AED", "COP",
                "SAR", "MYR", "RON", "PEN", "ARS", "UYU", "BOB", "PYG", "VES"
        };

        String upperCode = currencyCode.toUpperCase();
        for (String validCode : validCurrencies) {
            if (validCode.equals(upperCode)) {
                return true;
            }
        }

        return false;
    }
}