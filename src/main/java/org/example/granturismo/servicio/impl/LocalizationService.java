package org.example.granturismo.servicio.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.granturismo.dtos.LocalizedResponseDto;
import org.example.granturismo.dtos.PreferenceResponseDto;
import org.example.granturismo.dtos.TranslatedContentDto;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
                    .data(content)
                    .appliedLanguage("es")
                    .appliedCurrency("PEN")
                    .wasTranslated(false)
                    .wasCurrencyConverted(false)
                    .build();
        }

        // Obtener preferencias del usuario
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
                    .data(content)
                    .appliedLanguage(targetLanguage)
                    .appliedCurrency(targetCurrency)
                    .wasTranslated(false)
                    .wasCurrencyConverted(false)
                    .build();
        }

        boolean wasTranslated = false;
        boolean wasCurrencyConverted = false;

        try {
            // Clonar el objeto para no modificar el original
            T localizedContent = cloneObject(content);

            // Obtener metadatos del objeto
            String originalLanguage = extractFieldValue(content, "idiomaOriginal", String.class, "es");
            String originalCurrency = extractFieldValue(content, "monedaOriginal", String.class, "PEN");

            // Traducir textos si es necesario
            if (!originalLanguage.equalsIgnoreCase(targetLanguage)) {
                wasTranslated = translateTextFields(localizedContent, originalLanguage, targetLanguage);
            }

            // Convertir precios si es necesario
            if (!originalCurrency.equalsIgnoreCase(targetCurrency)) {
                wasCurrencyConverted = convertPriceFields(localizedContent, originalCurrency, targetCurrency);
            }

            log.info("Localización completada. Traducido: {}, Convertido: {}", wasTranslated, wasCurrencyConverted);

            return LocalizedResponseDto.<T>builder()
                    .data(localizedContent)
                    .appliedLanguage(targetLanguage)
                    .appliedCurrency(targetCurrency)
                    .wasTranslated(wasTranslated)
                    .wasCurrencyConverted(wasCurrencyConverted)
                    .build();

        } catch (Exception e) {
            log.error("Error durante la localización: {}", e.getMessage(), e);

            // En caso de error, retornar el contenido original
            return LocalizedResponseDto.<T>builder()
                    .data(content)
                    .appliedLanguage(targetLanguage)
                    .appliedCurrency(targetCurrency)
                    .wasTranslated(false)
                    .wasCurrencyConverted(false)
                    .build();
        }
    }

    /**
     * Traduce campos de texto en un objeto
     */
    private <T> boolean translateTextFields(T object, String sourceLanguage, String targetLanguage) {
        boolean wasTranslated = false;

        try {
            Class<?> clazz = object.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName().toLowerCase();

                // Campos que deben ser traducidos
                if (isTranslatableField(fieldName) && field.getType() == String.class) {
                    String originalText = (String) field.get(object);

                    if (originalText != null && !originalText.trim().isEmpty()) {
                        TranslatedContentDto translated = translationService.translateText(
                                originalText, sourceLanguage, targetLanguage);

                        if (translated != null && !originalText.equals(translated.getTranslatedText())) {
                            field.set(object, translated.getTranslatedText());
                            wasTranslated = true;
                            log.debug("Campo '{}' traducido de '{}' a '{}'",
                                    fieldName, originalText, translated.getTranslatedText());
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error al traducir campos de texto: {}", e.getMessage(), e);
        }

        return wasTranslated;
    }

    /**
     * Convierte campos de precio en un objeto
     */
    private <T> boolean convertPriceFields(T object, String sourceCurrency, String targetCurrency) {
        boolean wasCurrencyConverted = false;

        try {
            Class<?> clazz = object.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName().toLowerCase();

                // Campos que representan precios o montos
                if (isPriceField(fieldName) && isNumericType(field.getType())) {
                    Object originalValue = field.get(object);

                    if (originalValue != null) {
                        Double originalPrice = convertToDouble(originalValue);

                        if (originalPrice != null && originalPrice > 0) {
                            TranslatedContentDto converted = currencyConversionService.convertCurrency(
                                    originalPrice, sourceCurrency, targetCurrency);

                            if (converted != null && !originalPrice.equals(converted.getConvertedPrice())) {
                                setFieldValue(field, object, converted.getConvertedPrice());
                                wasCurrencyConverted = true;
                                log.debug("Campo '{}' convertido de {} {} a {} {}",
                                        fieldName, originalPrice, sourceCurrency,
                                        converted.getConvertedPrice(), targetCurrency);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error al convertir campos de precio: {}", e.getMessage(), e);
        }

        return wasCurrencyConverted;
    }

    /**
     * Determina si un campo debe ser traducido
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
     * Determina si un campo representa un precio
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
     * Verifica si un tipo es numérico
     */
    private boolean isNumericType(Class<?> type) {
        return type == Double.class || type == double.class ||
                type == Float.class || type == float.class ||
                type == Integer.class || type == int.class ||
                type == Long.class || type == long.class ||
                type == java.math.BigDecimal.class;
    }

    /**
     * Convierte un valor a Double
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
     * Establece el valor de un campo
     */
    private void setFieldValue(Field field, Object object, Double newValue) throws Exception {
        Class<?> fieldType = field.getType();

        if (fieldType == Double.class || fieldType == double.class) {
            field.set(object, newValue);
        } else if (fieldType == Float.class || fieldType == float.class) {
            field.set(object, newValue.floatValue());
        } else if (fieldType == Integer.class || fieldType == int.class) {
            field.set(object, newValue.intValue());
        } else if (fieldType == Long.class || fieldType == long.class) {
            field.set(object, newValue.longValue());
        } else if (fieldType == java.math.BigDecimal.class) {
            field.set(object, java.math.BigDecimal.valueOf(newValue));
        }
    }

    /**
     * Extrae el valor de un campo específico
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
            log.debug("No se pudo extraer campo '{}': {}", fieldName, e.getMessage());
        }

        return defaultValue;
    }

    /**
     * Clona un objeto (implementación básica)
     */
    @SuppressWarnings("unchecked")
    private <T> T cloneObject(T original) {
        try {
            // Intentar usar clone() si está disponible
            Method cloneMethod = original.getClass().getMethod("clone");
            return (T) cloneMethod.invoke(original);
        } catch (Exception e) {
            // Si no tiene clone(), usar reflexión para copiar campos
            try {
                T clone = (T) original.getClass().getDeclaredConstructor().newInstance();
                Field[] fields = original.getClass().getDeclaredFields();

                for (Field field : fields) {
                    field.setAccessible(true);
                    Object value = field.get(original);
                    field.set(clone, value);
                }

                return clone;
            } catch (Exception ex) {
                log.warn("No se pudo clonar el objeto, usando original: {}", ex.getMessage());
                return original;
            }
        }
    }
}