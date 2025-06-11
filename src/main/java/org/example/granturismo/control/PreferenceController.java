package org.example.granturismo.control;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.granturismo.dtos.PreferenceResponseDto;
import org.example.granturismo.dtos.PreferenceUpdateDto;
import org.example.granturismo.security.JwtTokenUtil;
import org.example.granturismo.servicio.impl.PreferenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/preferences")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Preferencias", description = "Gestión de preferencias de usuario (idioma y moneda)")
public class PreferenceController {

    private final PreferenceService preferenceService;
    private final JwtTokenUtil jwtTokenUtil; // Inject JwtTokenUtil


    /**
     * Extrae el ID del usuario del token JWT
     * Nota: Implementar según tu configuración de JWT
     */
    private Long getUserIdFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado"); // This would typically be handled by Spring Security for 401
        }

        // Get the JWT token from the request header (assuming it's passed as Bearer token)
        // This part needs to be handled by your JwtRequestFilter, which should store the token somewhere
        // or you need to re-extract it from the request if not already done.
        // The most robust way is to put the userId into CustomUserDetails during authentication.

        // --- OPTION 1: Best Practice - Get userId from CustomUserDetails if set as principal ---
        // Assuming your JwtRequestFilter sets a CustomUserDetails object as the principal.
        if (authentication.getPrincipal() instanceof org.example.granturismo.security.CustomUserDetails customUserDetails) {
            log.debug("Found CustomUserDetails in authentication principal. User ID: {}", customUserDetails.getIdUsuario());
            return customUserDetails.getIdUsuario();
        } else {
            // --- OPTION 2: If CustomUserDetails is not directly the principal, extract from token directly (less ideal) ---
            // This option requires you to somehow retrieve the raw JWT token from the request.
            // A more common approach is for JwtRequestFilter to already populate the Authentication object
            // with necessary user details (like the ID).

            // If the principal is the username (email) and the token is not readily available here,
            // then you need to redesign how the userId is passed to the controller.
            // However, since you have JwtTokenUtil, you could try to get it if the token is accessible.
            // This is generally not done in the controller, but rather by making the userId available
            // through the Authentication object itself or via a custom argument resolver.

            // For now, let's assume getUserIdFromToken() in JwtTokenUtil directly gives you the ID
            // if you passed the raw token to it. But the controller doesn't have the raw token.
            // Therefore, the safest way is through CustomUserDetails as in OPTION 1.

            // Fallback for debugging, but not recommended for production
            log.warn("Authentication principal is not CustomUserDetails. Trying to parse name (which is likely email). This will fail if name is not ID.");
            try {
                // If you *insist* on parsing authentication.getName(), ensure your JwtRequestFilter
                // sets authentication.getName() to the userId as a string. This is usually bad practice.
                return Long.parseLong(authentication.getName());
            } catch (NumberFormatException e) {
                log.error("Failed to parse user ID from authentication.getName(): {}. Original exception: {}", authentication.getName(), e.getMessage());
                throw new RuntimeException("No se pudo extraer el ID del usuario del token. El nombre del principal no es un ID numérico.", e);
            }
        }
    }

    @Operation(summary = "Obtener preferencias del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferencias obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/me")
    public ResponseEntity<PreferenceResponseDto> getMyPreferences() {
        Long userId = getUserIdFromToken();
        log.info("Obteniendo preferencias para usuario autenticado: {}", userId);

        PreferenceResponseDto preferences = preferenceService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    @Operation(summary = "Actualizar preferencias del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferencias actualizadas exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    @PutMapping("/me")
    public ResponseEntity<PreferenceResponseDto> updateMyPreferences(
            @Valid @RequestBody PreferenceUpdateDto updateDto) {
        Long userId = getUserIdFromToken();
        log.info("Actualizando preferencias para usuario autenticado: {}", userId);

        PreferenceResponseDto updatedPreferences = preferenceService.updateUserPreferences(userId, updateDto);
        return ResponseEntity.ok(updatedPreferences);
    }

    @Operation(summary = "Actualizar solo la moneda preferida")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Moneda actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Código de moneda inválido"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    @PatchMapping("/me/currency")
    public ResponseEntity<PreferenceResponseDto> updateMyCurrency(
            @Parameter(description = "Código de moneda ISO (ej: USD, EUR)", example = "PEN")
            @RequestParam String currencyCode) {
        Long userId = getUserIdFromToken();
        log.info("Actualizando moneda para usuario autenticado: {} a {}", userId, currencyCode);

        PreferenceResponseDto updatedPreferences = preferenceService.updateUserCurrency(userId, currencyCode);
        return ResponseEntity.ok(updatedPreferences);
    }

    @Operation(summary = "Actualizar solo el idioma preferido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Idioma actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Código de idioma inválido"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    @PatchMapping("/me/language")
    public ResponseEntity<PreferenceResponseDto> updateMyLanguage(
            @Parameter(description = "Código de idioma ISO (ej: es, en)", example = "es")
            @RequestParam String languageCode) {
        Long userId = getUserIdFromToken();
        log.info("Actualizando idioma para usuario autenticado: {} a {}", userId, languageCode);

        PreferenceResponseDto updatedPreferences = preferenceService.updateUserLanguage(userId, languageCode);
        return ResponseEntity.ok(updatedPreferences);
    }

    @Operation(summary = "Eliminar preferencias del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Preferencias eliminadas exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "404", description = "Preferencias no encontradas")
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyPreferences() {
        Long userId = getUserIdFromToken();
        log.info("Eliminando preferencias para usuario autenticado: {}", userId);

        preferenceService.deleteUserPreferences(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Crear preferencias por defecto para el usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Preferencias por defecto creadas"),
            @ApiResponse(responseCode = "400", description = "Ya existen preferencias para el usuario"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    @PostMapping("/me/default")
    public ResponseEntity<PreferenceResponseDto> createMyDefaultPreferences() {
        Long userId = getUserIdFromToken();
        log.info("Creando preferencias por defecto para usuario autenticado: {}", userId);

        PreferenceResponseDto preferences = preferenceService.createDefaultPreferences(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(preferences);
    }

    @Operation(summary = "Verificar si el usuario tiene preferencias configuradas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de preferencias obtenido"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    @GetMapping("/me/exists")
    public ResponseEntity<Map<String, Boolean>> checkMyPreferencesExist() {
        Long userId = getUserIdFromToken();
        log.info("Verificando existencia de preferencias para usuario autenticado: {}", userId);

        boolean hasPreferences = preferenceService.hasPreferences(userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasPreferences", hasPreferences);

        return ResponseEntity.ok(response);
    }

    // Endpoints administrativos (requieren rol ADMIN)

    @Operation(summary = "Obtener todas las preferencias (Admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de preferencias obtenida"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN")
    })
    @GetMapping("/admin/all")
    // @PreAuthorize("hasRole('ADMIN')") // Descomenta si tienes configurado Spring Security con roles
    public ResponseEntity<List<PreferenceResponseDto>> getAllPreferences() {
        log.info("Obteniendo todas las preferencias (endpoint administrativo)");

        List<PreferenceResponseDto> preferences = preferenceService.getAllPreferences();
        return ResponseEntity.ok(preferences);
    }

    @Operation(summary = "Obtener preferencias de un usuario específico (Admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferencias del usuario obtenidas"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/admin/user/{userId}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PreferenceResponseDto> getUserPreferences(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long userId) {
        log.info("Obteniendo preferencias para usuario: {} (endpoint administrativo)", userId);

        PreferenceResponseDto preferences = preferenceService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    @Operation(summary = "Actualizar preferencias de un usuario específico (Admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferencias actualizadas exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/admin/user/{userId}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PreferenceResponseDto> updateUserPreferences(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long userId,
            @Valid @RequestBody PreferenceUpdateDto updateDto) {
        log.info("Actualizando preferencias para usuario: {} (endpoint administrativo)", userId);

        PreferenceResponseDto updatedPreferences = preferenceService.updateUserPreferences(userId, updateDto);
        return ResponseEntity.ok(updatedPreferences);
    }

    @Operation(summary = "Eliminar preferencias de un usuario específico (Admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Preferencias eliminadas exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuario o preferencias no encontradas")
    })
    @DeleteMapping("/admin/user/{userId}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserPreferences(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long userId) {
        log.info("Eliminando preferencias para usuario: {} (endpoint administrativo)", userId);

        preferenceService.deleteUserPreferences(userId);
        return ResponseEntity.noContent().build();
    }

    // Métodos de utilidad



    // Endpoints de utilidad para el frontend

    @Operation(summary = "Obtener códigos de moneda soportados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de códigos de moneda obtenida")
    })
    @GetMapping("/currencies")
    public ResponseEntity<Map<String, String>> getSupportedCurrencies() {
        Map<String, String> currencies = new HashMap<>();
        currencies.put("USD", "Dólar Estadounidense");
        currencies.put("EUR", "Euro");
        currencies.put("GBP", "Libra Esterlina");
        currencies.put("JPY", "Yen Japonés");
        currencies.put("CAD", "Dólar Canadiense");
        currencies.put("AUD", "Dólar Australiano");
        currencies.put("CHF", "Franco Suizo");
        currencies.put("CNY", "Yuan Chino");
        currencies.put("MXN", "Peso Mexicano");
        currencies.put("BRL", "Real Brasileño");
        currencies.put("ARS", "Peso Argentino");
        currencies.put("CLP", "Peso Chileno");
        currencies.put("COP", "Peso Colombiano");
        currencies.put("PEN", "Sol Peruano");
        currencies.put("UYU", "Peso Uruguayo");

        return ResponseEntity.ok(currencies);
    }

    @Operation(summary = "Obtener códigos de idioma soportados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de códigos de idioma obtenida")
    })
    @GetMapping("/languages")
    public ResponseEntity<Map<String, String>> getSupportedLanguages() {
        Map<String, String> languages = new HashMap<>();
        languages.put("es", "Español");
        languages.put("en", "Inglés");
        languages.put("fr", "Francés");
        languages.put("de", "Alemán");
        languages.put("it", "Italiano");
        languages.put("pt", "Portugués");
        languages.put("ru", "Ruso");
        languages.put("ja", "Japonés");
        languages.put("ko", "Coreano");
        languages.put("zh", "Chino");
        languages.put("ar", "Árabe");
        languages.put("hi", "Hindi");
        languages.put("th", "Tailandés");
        languages.put("vi", "Vietnamita");
        languages.put("tr", "Turco");

        return ResponseEntity.ok(languages);
    }
}