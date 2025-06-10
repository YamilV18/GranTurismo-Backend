package org.example.granturismo.servicio.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.granturismo.dtos.PreferenceResponseDto;
import org.example.granturismo.dtos.PreferenceUpdateDto;
import org.example.granturismo.excepciones.ExcepcionPersonalizada;
import org.example.granturismo.mappers.PreferenceMapper;
import org.example.granturismo.modelo.Preference;
import org.example.granturismo.repositorio.PreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PreferenceService {

    private final PreferenceRepository preferenceRepository;
    private final PreferenceMapper preferenceMapper;
    private final CurrencyConversionService currencyConversionService;

    // Valores por defecto
    private static final String DEFAULT_CURRENCY = "PEN";
    private static final String DEFAULT_LANGUAGE = "es";

    /**
     * Obtiene las preferencias de un usuario, creando valores por defecto si no existen
     */
    @Transactional(readOnly = true)
    public PreferenceResponseDto getUserPreferences(Long userId) {
        log.info("Obteniendo preferencias para usuario: {}", userId);

        if (userId == null) {
            throw new ExcepcionPersonalizada("ID de usuario es requerido");
        }

        Optional<Preference> preferenceOpt = preferenceRepository.findByIdUsuario(userId);

        if (preferenceOpt.isPresent()) {
            log.info("Preferencias encontradas para usuario: {}", userId);
            return preferenceMapper.toResponseDto(preferenceOpt.get());
        } else {
            log.info("No se encontraron preferencias para usuario: {}. Creando preferencias por defecto", userId);
            return createDefaultPreferences(userId);
        }
    }

    /**
     * Crea preferencias por defecto para un usuario
     */
    public PreferenceResponseDto createDefaultPreferences(Long userId) {
        log.info("Creando preferencias por defecto para usuario: {}", userId);

        if (userId == null) {
            throw new ExcepcionPersonalizada("ID de usuario es requerido");
        }

        // Verificar si ya existen preferencias
        if (preferenceRepository.existsByIdUsuario(userId)) {
            log.warn("Ya existen preferencias para el usuario: {}", userId);
            return getUserPreferences(userId);
        }

        Preference defaultPreference = preferenceMapper.createDefaultPreference(userId);
        Preference savedPreference = preferenceRepository.save(defaultPreference);

        log.info("Preferencias por defecto creadas para usuario: {}", userId);
        return preferenceMapper.toResponseDto(savedPreference);
    }

    /**
     * Actualiza las preferencias de un usuario
     */
    public PreferenceResponseDto updateUserPreferences(Long userId, PreferenceUpdateDto updateDto) {
        log.info("Actualizando preferencias para usuario: {}", userId);

        if (userId == null) {
            throw new ExcepcionPersonalizada("ID de usuario es requerido");
        }

        if (updateDto == null) {
            throw new ExcepcionPersonalizada("Datos de actualización son requeridos");
        }

        // Validar código de moneda
        if (!currencyConversionService.isValidCurrencyCode(updateDto.getPreferredCurrencyCode())) {
            throw new ExcepcionPersonalizada("Código de moneda inválido: " + updateDto.getPreferredCurrencyCode());
        }

        // Validar código de idioma
        if (!isValidLanguageCode(updateDto.getPreferredLanguageCode())) {
            throw new ExcepcionPersonalizada("Código de idioma inválido: " + updateDto.getPreferredLanguageCode());
        }

        Optional<Preference> preferenceOpt = preferenceRepository.findByIdUsuario(userId);

        Preference preference;
        if (preferenceOpt.isPresent()) {
            // Actualizar preferencias existentes
            preference = preferenceOpt.get();
            preferenceMapper.updateEntityFromDto(updateDto, preference);
            log.info("Actualizando preferencias existentes para usuario: {}", userId);
        } else {
            // Crear nuevas preferencias
            preference = preferenceMapper.toEntity(updateDto);
            preference.setIdUsuario(userId);
            log.info("Creando nuevas preferencias para usuario: {}", userId);
        }

        Preference savedPreference = preferenceRepository.save(preference);
        log.info("Preferencias guardadas exitosamente para usuario: {}", userId);

        return preferenceMapper.toResponseDto(savedPreference);
    }

    /**
     * Elimina las preferencias de un usuario
     */
    public void deleteUserPreferences(Long userId) {
        log.info("Eliminando preferencias para usuario: {}", userId);

        if (userId == null) {
            throw new ExcepcionPersonalizada("ID de usuario es requerido");
        }

        if (!preferenceRepository.existsByIdUsuario(userId)) {
            log.warn("No existen preferencias para eliminar para usuario: {}", userId);
            throw new ExcepcionPersonalizada("No se encontraron preferencias para el usuario");
        }

        preferenceRepository.deleteByIdUsuario(userId);
        log.info("Preferencias eliminadas exitosamente para usuario: {}", userId);
    }

    /**
     * Obtiene todas las preferencias (solo para administradores)
     */
    @Transactional(readOnly = true)
    public List<PreferenceResponseDto> getAllPreferences() {
        log.info("Obteniendo todas las preferencias");

        List<Preference> preferences = preferenceRepository.findAll();
        return preferences.stream()
                .map(preferenceMapper::toResponseDto)
                .toList();
    }

    /**
     * Obtiene las preferencias con información del usuario
     */
    @Transactional(readOnly = true)
    public PreferenceResponseDto getUserPreferencesWithUserInfo(Long userId) {
        log.info("Obteniendo preferencias con información de usuario: {}", userId);

        if (userId == null) {
            throw new ExcepcionPersonalizada("ID de usuario es requerido");
        }

        Optional<Preference> preferenceOpt = preferenceRepository.findByIdUsuarioWithUser(userId);

        if (preferenceOpt.isPresent()) {
            return preferenceMapper.toResponseDto(preferenceOpt.get());
        } else {
            log.info("No se encontraron preferencias para usuario: {}. Creando preferencias por defecto", userId);
            return createDefaultPreferences(userId);
        }
    }

    /**
     * Verifica si existen preferencias para un usuario
     */
    @Transactional(readOnly = true)
    public boolean hasPreferences(Long userId) {
        if (userId == null) {
            return false;
        }
        return preferenceRepository.existsByIdUsuario(userId);
    }

    /**
     * Obtiene solo el código de moneda preferido de un usuario
     */
    @Transactional(readOnly = true)
    public String getUserPreferredCurrency(Long userId) {
        PreferenceResponseDto preferences = getUserPreferences(userId);
        return preferences.getPreferredCurrencyCode();
    }

    /**
     * Obtiene solo el código de idioma preferido de un usuario
     */
    @Transactional(readOnly = true)
    public String getUserPreferredLanguage(Long userId) {
        PreferenceResponseDto preferences = getUserPreferences(userId);
        return preferences.getPreferredLanguageCode();
    }

    /**
     * Actualiza solo la moneda preferida
     */
    public PreferenceResponseDto updateUserCurrency(Long userId, String currencyCode) {
        log.info("Actualizando moneda preferida para usuario: {} a {}", userId, currencyCode);

        if (!currencyConversionService.isValidCurrencyCode(currencyCode)) {
            throw new ExcepcionPersonalizada("Código de moneda inválido: " + currencyCode);
        }

        PreferenceResponseDto currentPreferences = getUserPreferences(userId);

        PreferenceUpdateDto updateDto = PreferenceUpdateDto.builder()
                .preferredCurrencyCode(currencyCode.toUpperCase())
                .preferredLanguageCode(currentPreferences.getPreferredLanguageCode())
                .build();

        return updateUserPreferences(userId, updateDto);
    }

    /**
     * Actualiza solo el idioma preferido
     */
    public PreferenceResponseDto updateUserLanguage(Long userId, String languageCode) {
        log.info("Actualizando idioma preferido para usuario: {} a {}", userId, languageCode);

        if (!isValidLanguageCode(languageCode)) {
            throw new ExcepcionPersonalizada("Código de idioma inválido: " + languageCode);
        }

        PreferenceResponseDto currentPreferences = getUserPreferences(userId);

        PreferenceUpdateDto updateDto = PreferenceUpdateDto.builder()
                .preferredCurrencyCode(currentPreferences.getPreferredCurrencyCode())
                .preferredLanguageCode(languageCode.toLowerCase())
                .build();

        return updateUserPreferences(userId, updateDto);
    }

    /**
     * Valida si un código de idioma es válido
     */
    private boolean isValidLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            return false;
        }

        String cleanCode = languageCode.trim().toLowerCase();

        // Códigos de idioma válidos (ISO 639-1 y algunos regionales)
        String[] validLanguages = {
                "es", "en", "fr", "de", "it", "pt", "ru", "ja", "ko", "zh",
                "ar", "hi", "th", "vi", "tr", "pl", "nl", "sv", "da", "no",
                "fi", "cs", "hu", "ro", "bg", "hr", "sk", "sl", "et", "lv",
                "lt", "mt", "ca", "eu", "gl", "cy", "ga", "is", "mk", "sq",
                "sr", "bs", "me", "hr", "si", "ta", "te", "kn", "ml", "pa",
                "gu", "or", "as", "bn", "ur", "fa", "he", "am", "sw", "zu",
                "af", "xh", "st", "tn", "ss", "ve", "ts", "nr", "ee", "ff",
                "ha", "ig", "yo", "wo", "ak", "bm", "lg", "sn", "rw", "ny",
                "es-es", "es-mx", "es-ar", "es-co", "es-pe", "es-cl", "es-ve",
                "en-us", "en-gb", "en-au", "en-ca", "en-in", "en-za",
                "pt-br", "pt-pt", "fr-fr", "fr-ca", "de-de", "de-at", "de-ch",
                "it-it", "ru-ru", "zh-cn", "zh-tw", "ar-sa", "ar-ae"
        };

        for (String validCode : validLanguages) {
            if (validCode.equals(cleanCode)) {
                return true;
            }
        }

        return false;
    }
}
