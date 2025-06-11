package org.example.granturismo.servicio.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.granturismo.dtos.LocalizedResponseDto;
import org.example.granturismo.dtos.PreferenceResponseDto;
import org.example.granturismo.dtos.TranslatedContentDto;
import org.example.granturismo.dtos.PaqueteDTO; // Add this import explicitly for clarity
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal; // Import BigDecimal
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalizationService {

    private final PreferenceService preferenceService;
    private final TranslationService translationService;
    private final CurrencyConversionService currencyConversionService;

    /**
     * Localiza cualquier objeto basado en las preferencias del usuario
     */
    public <T> LocalizedResponseDto<T> localizeContent(T content, Long userId) {
        log.info("Iniciando localización de contenido para usuario: {}", userId);

        if (content == null) {
            return LocalizedResponseDto.<T>builder()
                    .data(null) // Data is null if content is null
                    .appliedLanguage("es") // Default language if no preferences or content
                    .appliedCurrency("PEN") // Default currency if no preferences or content
                    .wasTranslated(false)
                    .wasCurrencyConverted(false)
                    .exchangeRate(1.0) // Default exchange rate for no conversion
                    .build();
        }

        PreferenceResponseDto preferences = preferenceService.getUserPreferences(userId);

        return localizeContent(content, preferences.getPreferredLanguageCode(),
                preferences.getPreferredCurrencyCode());
    }

    /**
     * Localiza contenido con idioma y moneda específicos
     */
    public <T> LocalizedResponseDto<T> localizeContent(T content, String targetLanguage, String targetCurrency) {
        log.info("Localizando contenido. Idioma: {}, Moneda: {}", targetLanguage, targetCurrency);

        if (content == null) {
            return LocalizedResponseDto.<T>builder()
                    .data(null) // Data is null if content is null
                    .appliedLanguage(targetLanguage)
                    .appliedCurrency(targetCurrency)
                    .wasTranslated(false)
                    .wasCurrencyConverted(false)
                    .exchangeRate(1.0) // Default exchange rate
                    .build();
        }

        boolean wasTranslated = false;
        boolean wasCurrencyConverted = false;
        Double finalExchangeRate = 1.0; // Default exchange rate

        try {
            // Deep clone the object to ensure all nested objects are also new instances
            T localizedContent = deepCloneObject(content);

            // Get metadata from the *original* object to determine source language/currency
            String originalLanguage = extractFieldValue(content, "idiomaOriginal", String.class, "es");
            String originalCurrency = extractFieldValue(content, "monedaOriginal", String.class, "PEN");

            // Translate texts if necessary on the *cloned* object
            if (!originalLanguage.equalsIgnoreCase(targetLanguage)) {
                wasTranslated = translateFieldsRecursively(localizedContent, originalLanguage, targetLanguage);
            }

            // Convert prices if necessary on the *cloned* object
            if (!originalCurrency.equalsIgnoreCase(targetCurrency)) {
                // Perform conversion and get the rate
                wasCurrencyConverted = convertPriceFieldsRecursively(localizedContent, originalCurrency, targetCurrency);

                // Assuming you have a way to get the actual exchange rate used for the main price.
                // If convertPriceFieldsRecursively returns the rate, use it. Otherwise, you need to
                // call currencyConversionService.convertCurrency for the main price here to get the rate.
                // For simplicity, let's call it for the main price if it's a PaqueteDTO
                if (localizedContent instanceof PaqueteDTO paqueteDTO) {
                    BigDecimal originalMainPrice = extractFieldValue(content, "precioTotal", BigDecimal.class, BigDecimal.ZERO);
                    if (originalMainPrice != null && originalMainPrice.compareTo(BigDecimal.ZERO) > 0) {
                        TranslatedContentDto conversionResult = currencyConversionService.convertCurrency(
                                originalMainPrice.doubleValue(), originalCurrency, targetCurrency);
                        if (conversionResult != null && conversionResult.getExchangeRate() != null) {
                            finalExchangeRate = conversionResult.getExchangeRate();
                        }
                    }
                }
                // If it's a PaqueteListDTO, do similarly for its precioTotal
                else if (localizedContent instanceof PaqueteDTO.PaqueteListDTO paqueteListDTO) {
                    BigDecimal originalMainPrice = extractFieldValue(content, "precioTotal", BigDecimal.class, BigDecimal.ZERO);
                    if (originalMainPrice != null && originalMainPrice.compareTo(BigDecimal.ZERO) > 0) {
                        TranslatedContentDto conversionResult = currencyConversionService.convertCurrency(
                                originalMainPrice.doubleValue(), originalCurrency, targetCurrency);
                        if (conversionResult != null && conversionResult.getExchangeRate() != null) {
                            finalExchangeRate = conversionResult.getExchangeRate();
                        }
                    }
                }
            }

            log.info("Localización completada. Traducido: {}, Convertido: {}", wasTranslated, wasCurrencyConverted);

            return LocalizedResponseDto.<T>builder()
                    .data(localizedContent) // Return the modified, localized content
                    .appliedLanguage(targetLanguage)
                    .appliedCurrency(targetCurrency)
                    .wasTranslated(wasTranslated)
                    .wasCurrencyConverted(wasCurrencyConverted)
                    .exchangeRate(finalExchangeRate) // Set the final exchange rate here
                    .build();

        } catch (Exception e) {
            log.error("Error during localization: {}", e.getMessage(), e);

            // In case of error, return the original content and indicate no localization happened
            return LocalizedResponseDto.<T>builder()
                    .data(content)
                    .appliedLanguage(targetLanguage)
                    .appliedCurrency(targetCurrency)
                    .wasTranslated(false)
                    .wasCurrencyConverted(false)
                    .exchangeRate(1.0) // Default rate if error
                    .build();
        }
    }

    /**
     * Recursively translates text fields within an object and its nested objects.
     * (No changes needed here, as it modifies the `contentDto` passed in `localizedContent`)
     */
    private <T> boolean translateFieldsRecursively(T object, String sourceLanguage, String targetLanguage) {
        if (object == null) {
            return false;
        }

        boolean translated = false;
        Class<?> clazz = object.getClass();

        // Handle direct fields of the current object
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName().toLowerCase();

            try {
                if (isTranslatableField(fieldName) && field.getType() == String.class) {
                    String originalText = (String) field.get(object);
                    if (originalText != null && !originalText.trim().isEmpty()) {
                        TranslatedContentDto translatedDto = translationService.translateText(
                                originalText, sourceLanguage, targetLanguage);

                        if (translatedDto != null && !originalText.equals(translatedDto.getTranslatedText())) {
                            field.set(object, translatedDto.getTranslatedText());
                            translated = true;
                            log.debug("Field '{}' translated from '{}' to '{}'",
                                    fieldName, originalText, translatedDto.getTranslatedText());
                        }
                    }
                } else if (isComplexObject(field.getType())) {
                    // Recursively translate fields in nested complex objects (DTOs)
                    Object nestedObject = field.get(object);
                    if (nestedObject != null) {
                        if (translateFieldsRecursively(nestedObject, sourceLanguage, targetLanguage)) {
                            translated = true;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error translating field '{}' in object of type {}: {}",
                        fieldName, clazz.getName(), e.getMessage());
            }
        }
        return translated;
    }

    /**
     * Recursively converts currency fields within an object and its nested objects.
     * (No changes needed here, as it modifies the `contentDto` passed in `localizedContent`)
     */
    private <T> boolean convertPriceFieldsRecursively(T object, String sourceCurrency, String targetCurrency) {
        if (object == null) {
            return false;
        }

        boolean converted = false;
        Class<?> clazz = object.getClass();

        // Handle direct fields of the current object
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName().toLowerCase();

            try {
                if (isPriceField(fieldName) && isNumericType(field.getType())) {
                    Object originalValue = field.get(object);

                    if (originalValue != null) {
                        // Correctly handle BigDecimal to Double for conversion service
                        Double originalPrice = null;
                        if (originalValue instanceof BigDecimal) {
                            originalPrice = ((BigDecimal) originalValue).doubleValue();
                        } else if (originalValue instanceof Number) {
                            originalPrice = ((Number) originalValue).doubleValue();
                        }

                        if (originalPrice != null && originalPrice > 0) {
                            TranslatedContentDto convertedDto = currencyConversionService.convertCurrency(
                                    originalPrice, sourceCurrency, targetCurrency);

                            if (convertedDto != null && convertedDto.getConvertedPrice() != null &&
                                    !originalPrice.equals(convertedDto.getConvertedPrice())) {
                                // Set the converted value back to the field, handling BigDecimal specifically
                                setNumericFieldValue(field, object, BigDecimal.valueOf(convertedDto.getConvertedPrice()));
                                converted = true;
                                log.debug("Field '{}' converted from {} {} to {} {}",
                                        fieldName, originalPrice, sourceCurrency,
                                        convertedDto.getConvertedPrice(), targetCurrency);
                            }
                        }
                    }
                } else if (isComplexObject(field.getType())) {
                    // Recursively convert prices in nested complex objects (DTOs)
                    Object nestedObject = field.get(object);
                    if (nestedObject != null) {
                        if (convertPriceFieldsRecursively(nestedObject, sourceCurrency, targetCurrency)) {
                            converted = true;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error converting currency field '{}' in object of type {}: {}",
                        fieldName, clazz.getName(), e.getMessage());
            }
        }
        return converted;
    }

    /**
     * Determines if a field should be translated.
     */
    private boolean isTranslatableField(String fieldName) {
        String[] translatableFields = {
                "nombre", "titulo", "title", "descripcion", "description",
                "contenido", "content", "resumen", "summary", "detalle",
                "detail", "comentario", "comment", "observacion", "observation",
                "instruccion", "instruction", "nota", "note", "mensaje", "message"
        };

        for (String translatableField : translatableFields) {
            if (fieldName.contains(translatableField)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a field represents a price.
     */
    private boolean isPriceField(String fieldName) {
        String[] priceFields = {
                "precio", "price", "costo", "cost", "tarifa", "fare", "monto",
                "amount", "valor", "value", "total", "subtotal", "descuento",
                "discount", "impuesto", "tax", "comision", "commission"
        };

        for (String priceField : priceFields) {
            if (fieldName.contains(priceField)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a type is numeric.
     */
    private boolean isNumericType(Class<?> type) {
        return type == Double.class || type == double.class ||
                type == Float.class || type == float.class ||
                type == Integer.class || type == int.class ||
                type == Long.class || type == long.class ||
                type == java.math.BigDecimal.class;
    }

    /**
     * Checks if a type is a complex object (not a primitive, String, or known wrapper).
     */
    private boolean isComplexObject(Class<?> type) {
        return !type.isPrimitive() &&
                !type.isArray() &&
                !type.isEnum() &&
                !type.isInterface() &&
                !type.equals(String.class) &&
                !Number.class.isAssignableFrom(type) &&
                !Boolean.class.isAssignableFrom(type) &&
                !type.equals(LocalDateTime.class) &&
                !type.equals(LocalDate.class) &&
                !type.equals(LocalTime.class) &&
                !type.equals(java.util.Date.class) &&
                !type.getName().startsWith("java.lang.");
    }

    /**
     * Converts a value to Double.
     * (No changes needed here as it's a helper for extractFieldValue/convertPriceFieldsRecursively)
     */
    private Double convertToDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Sets the value of a specific field.
     * Modified to accept Number to handle BigDecimal properly.
     */
    private void setFieldValue(Field field, Object object, Number newValue) throws Exception {
        Class<?> fieldType = field.getType();
        if (fieldType == Double.class || fieldType == double.class) {
            field.set(object, newValue.doubleValue());
        } else if (fieldType == Float.class || fieldType == float.class) {
            field.set(object, newValue.floatValue());
        } else if (fieldType == Integer.class || fieldType == int.class) {
            field.set(object, newValue.intValue());
        } else if (fieldType == Long.class || fieldType == long.class) {
            field.set(object, newValue.longValue());
        } else if (fieldType == java.math.BigDecimal.class) {
            field.set(object, java.math.BigDecimal.valueOf(newValue.doubleValue())); // Convert to BigDecimal
        } else {
            // Fallback for other numeric types if needed, or throw error for unsupported
            throw new IllegalArgumentException("Unsupported numeric type for field " + field.getName() + ": " + fieldType.getName());
        }
    }

    /**
     * Sets the value of a field by name.
     * (No changes needed here, as it calls `setNumericFieldValue` which was updated)
     */
    private void setFieldValue(Object object, String fieldName, Object newValue) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            // Check if the new value is compatible with the field type
            if (field.getType().isInstance(newValue) || (newValue == null && !field.getType().isPrimitive())) {
                field.set(object, newValue);
            } else if (newValue instanceof Number && isNumericType(field.getType())) {
                setNumericFieldValue(field, object, (Number) newValue);
            } else {
                log.warn("Attempted to set incompatible value for field '{}' in class {}. Expected type: {}, Provided type: {}",
                        fieldName, object.getClass().getName(), field.getType().getName(), newValue != null ? newValue.getClass().getName() : "null");
            }
        } catch (NoSuchFieldException e) {
            // This is expected and acceptable if the field (like monedaAplicada in PaqueteDTO) was removed
            log.debug("Field '{}' not found in class {}", fieldName, object.getClass().getName());
        } catch (Exception e) {
            log.error("Error setting field '{}' to value '{}' in object of type {}: {}",
                    fieldName, newValue, object.getClass().getName(), e.getMessage());
        }
    }

    private void setNumericFieldValue(Field field, Object object, Number newValue) throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        if (fieldType == Double.class || fieldType == double.class) {
            field.set(object, newValue.doubleValue());
        } else if (fieldType == Float.class || fieldType == float.class) {
            field.set(object, newValue.floatValue());
        } else if (fieldType == Integer.class || fieldType == int.class) {
            field.set(object, newValue.intValue());
        } else if (fieldType == Long.class || fieldType == long.class) {
            field.set(object, newValue.longValue());
        } else if (fieldType == java.math.BigDecimal.class) {
            field.set(object, java.math.BigDecimal.valueOf(newValue.doubleValue())); // Convert to BigDecimal
        } else {
            field.set(object, newValue);
        }
    }

    /**
     * Extracts the value of a specific field.
     * (No changes needed here as it's a helper)
     */
    @SuppressWarnings("unchecked")
    private <V> V extractFieldValue(Object object, String fieldName, Class<V> expectedType, V defaultValue) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(object);

            if (value != null && expectedType.isInstance(value)) {
                return (V) value;
            }
        } catch (Exception e) {
            log.debug("Could not extract field '{}': {}", fieldName, e.getMessage());
        }
        return defaultValue;
    }

    /**
     * Deep clones an object using reflection.
     * (No changes needed here - this method handles immutable types and complex objects)
     */
    @SuppressWarnings("unchecked")
    private <T> T deepCloneObject(T original) throws InstantiationException, IllegalAccessException, NoSuchMethodException, java.lang.reflect.InvocationTargetException {
        if (original == null) {
            return null;
        }

        Class<?> clazz = original.getClass();

        if (clazz.isPrimitive() || clazz == String.class || Number.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz)) {
            return original;
        }

        if (clazz.equals(LocalDateTime.class) || clazz.equals(LocalDate.class) || clazz.equals(LocalTime.class)) {
            return original;
        }
        if (clazz.equals(java.util.Date.class)) {
            return original;
        }

        T clone;
        try {
            clone = (T) clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            log.warn("Class {} does not have a no-arg constructor. Cannot deep clone. Returning original object.", clazz.getName());
            return original;
        }

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(original);
            if (value != null) {
                if (isComplexObject(field.getType())) {
                    field.set(clone, deepCloneObject(value));
                } else {
                    field.set(clone, value);
                }
            }
        }
        return clone;
    }
}