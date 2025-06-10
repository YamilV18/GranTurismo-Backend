package org.example.granturismo.servicio.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.granturismo.dtos.TranslatedContentDto;
import org.example.granturismo.excepciones.ExcepcionPersonalizada;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TranslationService {

    @Value("${deepl.api.key:}")
    private String deeplApiKey;

    @Value("${deepl.api.url:https://api-free.deepl.com/v2/translate}")
    private String deeplApiUrl;

    private final RestTemplate restTemplate;

    public TranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Traduce un texto usando DeepL API
     */
    public TranslatedContentDto translateText(String text, String sourceLanguage, String targetLanguage) {
        log.info("Iniciando traducción de texto. Idioma origen: {}, idioma destino: {}", sourceLanguage, targetLanguage);

        if (!StringUtils.hasText(deeplApiKey)) {
            log.warn("API Key de DeepL no configurada. Retornando texto original.");
            return TranslatedContentDto.builder()
                    .originalText(text)
                    .translatedText(text)
                    .sourceLanguage(sourceLanguage)
                    .targetLanguage(targetLanguage)
                    .build();
        }

        if (!StringUtils.hasText(text)) {
            log.warn("Texto vacío para traducir");
            return TranslatedContentDto.builder()
                    .originalText(text)
                    .translatedText(text)
                    .sourceLanguage(sourceLanguage)
                    .targetLanguage(targetLanguage)
                    .build();
        }

        // Si el idioma origen y destino son iguales, no traducir
        if (sourceLanguage.equalsIgnoreCase(targetLanguage)) {
            log.info("Idiomas origen y destino son iguales. No se requiere traducción.");
            return TranslatedContentDto.builder()
                    .originalText(text)
                    .translatedText(text)
                    .sourceLanguage(sourceLanguage)
                    .targetLanguage(targetLanguage)
                    .build();
        }

        try {
            // Preparar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "DeepL-Auth-Key " + deeplApiKey);

            // Preparar parámetros
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("text", text);
            body.add("source_lang", mapLanguageCode(sourceLanguage));
            body.add("target_lang", mapLanguageCode(targetLanguage));

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            // Realizar petición
            ResponseEntity<Map> response = restTemplate.exchange(
                    deeplApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> translations = (List<Map<String, Object>>) responseBody.get("translations");

                if (translations != null && !translations.isEmpty()) {
                    String translatedText = (String) translations.get(0).get("text");
                    String detectedSourceLang = (String) translations.get(0).get("detected_source_language");

                    log.info("Traducción completada exitosamente");

                    return TranslatedContentDto.builder()
                            .originalText(text)
                            .translatedText(translatedText)
                            .sourceLanguage(detectedSourceLang != null ? detectedSourceLang.toLowerCase() : sourceLanguage)
                            .targetLanguage(targetLanguage)
                            .build();
                }
            }

            log.warn("Respuesta de DeepL API sin traducciones válidas");
            throw new ExcepcionPersonalizada("No se pudo obtener la traducción del texto");

        } catch (Exception e) {
            log.error("Error al traducir texto: {}", e.getMessage(), e);

            // En caso de error, retornar el texto original
            return TranslatedContentDto.builder()
                    .originalText(text)
                    .translatedText(text)
                    .sourceLanguage(sourceLanguage)
                    .targetLanguage(targetLanguage)
                    .build();
        }
    }

    /**
     * Mapea códigos de idioma a formato DeepL
     */
    private String mapLanguageCode(String languageCode) {
        if (languageCode == null) return "ES";

        String upperCode = languageCode.toUpperCase();

        // Mapeo de códigos comunes
        switch (upperCode) {
            case "ES":
            case "ES-ES":
                return "ES";
            case "EN":
            case "EN-US":
            case "EN-GB":
                return "EN-US";
            case "FR":
            case "FR-FR":
                return "FR";
            case "DE":
            case "DE-DE":
                return "DE";
            case "IT":
            case "IT-IT":
                return "IT";
            case "PT":
            case "PT-PT":
                return "PT-PT";
            case "PT-BR":
                return "PT-BR";
            case "RU":
                return "RU";
            case "JA":
                return "JA";
            case "ZH":
            case "ZH-CN":
                return "ZH";
            default:
                // Si no está mapeado, intentar usar el código tal como está
                return upperCode.split("-")[0];
        }
    }

    /**
     * Verifica si el servicio de traducción está disponible
     */
    public boolean isTranslationServiceAvailable() {
        return StringUtils.hasText(deeplApiKey);
    }
}