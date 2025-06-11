package org.example.granturismo.control;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.granturismo.dtos.LocalizedResponseDto; // Import this
import org.example.granturismo.dtos.PaqueteDTO;
import org.example.granturismo.mappers.PaqueteMapper;
import org.example.granturismo.modelo.Paquete;
import org.example.granturismo.security.CustomUserDetails;
import org.example.granturismo.security.PermitRoles;
import org.example.granturismo.servicio.impl.PaqueteServiceImp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/paquetes")
@Slf4j
@Tag(name = "Paquetes Turísticos", description = "Gestión de paquetes turísticos con localización automática")
public class PaqueteController {

    private final PaqueteServiceImp paqueteService;
    private final PaqueteMapper paqueteMapper; // Keep this for admin/original endpoints

    // ENDPOINTS ORIGINALES (SIN LOCALIZACIÓN) - Para compatibilidad
    // These methods remain unchanged as they are intended to return original data.

    @GetMapping("/admin/all")
    @PermitRoles({"ADMIN"})
    @Operation(summary = "Obtener todos los paquetes sin localización (Admin)")
    public ResponseEntity<List<PaqueteDTO>> findAll() {
        List<Paquete> paquetes = paqueteService.findAll();
        List<PaqueteDTO> list = paqueteMapper.toDTOs(paquetes);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/admin/{id}")
    @PermitRoles({"ADMIN"})
    @Operation(summary = "Obtener paquete por ID sin localización (Admin)")
    public ResponseEntity<PaqueteDTO> findByIdAdmin(@PathVariable("id") Long id) {
        Paquete obj = paqueteService.findById(id);
        return ResponseEntity.ok(paqueteMapper.toDTO(obj));
    }

    @PostMapping
    @PermitRoles({"ADMIN"})
    @Operation(summary = "Crear nuevo paquete")
    public ResponseEntity<?> save(
            @Valid @RequestPart("dto") PaqueteDTO.PaqueteCADTO dto,
            @RequestPart("imagenFile") MultipartFile imagenFile) {
        try {
            PaqueteDTO obj = paqueteService.saveD(dto, imagenFile);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}").buildAndExpand(obj.getIdPaquete()).toUri();
            return ResponseEntity.created(location).body(obj);
        } catch (IOException e) {
            log.error("Error de E/S al procesar la imagen: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error de E/S al procesar la imagen: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al crear el paquete: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ocurrió un error al crear el paquete: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PermitRoles({"ADMIN", "PROV"})
    @Operation(summary = "Actualizar paquete existente")
    public ResponseEntity<?> update(
            @PathVariable("id") Long id,
            @Valid @RequestPart("dto") PaqueteDTO.PaqueteCADTO dto,
            @RequestPart(value = "imagenFile", required = false) MultipartFile imagenFile) {
        try {
            PaqueteDTO obj = paqueteService.updateD(dto, id, imagenFile);
            return ResponseEntity.ok(obj);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            log.error("Error de E/S al procesar la imagen al actualizar: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error de E/S al procesar la imagen al actualizar: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar el paquete: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ocurrió un error al actualizar el paquete: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PermitRoles({"ADMIN"})
    @Operation(summary = "Eliminar paquete")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        try {
            paqueteService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al eliminar el paquete: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar el paquete: " + e.getMessage()));
        }
    }

    @GetMapping("/admin/pageable")
    @PermitRoles({"ADMIN"})
    @Operation(summary = "Listar paquetes paginados sin localización (Admin)")
    public ResponseEntity<Page<PaqueteDTO>> listPageAdmin(Pageable pageable) {
        Page<PaqueteDTO> page = paqueteService.listaPage(pageable).map(paqueteMapper::toDTO);
        return ResponseEntity.ok(page);
    }

    // NUEVOS ENDPOINTS CON LOCALIZACIÓN AUTOMÁTICA

    @GetMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    @Operation(summary = "Obtener paquete específico localizado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paquete obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Paquete no encontrado"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    public ResponseEntity<LocalizedResponseDto<PaqueteDTO>> obtenerPaqueteLocalizado( // <-- Changed return type
                                                                                      @Parameter(description = "ID del paquete", example = "1")
                                                                                      @PathVariable Long id) {
        Long userId = getUserIdFromToken();
        log.info("Obteniendo paquete {} localizado para usuario {}", id, userId);

        // Now the service returns LocalizedResponseDto<PaqueteDTO>
        LocalizedResponseDto<PaqueteDTO> localizedResponse = paqueteService.getPaqueteLocalizado(id, userId);
        return ResponseEntity.ok(localizedResponse); // <-- Return the entire LocalizedResponseDto
    }

    @GetMapping
    @PermitRoles({"ADMIN", "USER", "PROV"})
    @Operation(summary = "Obtener lista paginada de paquetes localizados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de paquetes obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    public ResponseEntity<Page<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>>> listarPaquetesLocalizados( // <-- Changed return type
                                                                                                            @Parameter(description = "Número de página (0-indexed)", example = "0")
                                                                                                            @RequestParam(defaultValue = "0") int page,
                                                                                                            @Parameter(description = "Tamaño de página", example = "10")
                                                                                                            @RequestParam(defaultValue = "10") int size,
                                                                                                            @Parameter(description = "Campo de ordenamiento", example = "titulo")
                                                                                                            @RequestParam(defaultValue = "titulo") String sortBy,
                                                                                                            @Parameter(description = "Dirección de ordenamiento", example = "ASC")
                                                                                                            @RequestParam(defaultValue = "ASC") String sortDir) {

        Long userId = getUserIdFromToken();
        log.info("Listando paquetes localizados para usuario {} - página: {}, tamaño: {}", userId, page, size);

        Sort.Direction direction = sortDir.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Now the service returns Page<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>>
        Page<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>> paquetes = paqueteService.getPaquetesLocalizados(userId, pageable);
        return ResponseEntity.ok(paquetes); // <-- Return the entire Page
    }

    @GetMapping("/estado/{estado}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    @Operation(summary = "Buscar paquetes por estado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paquetes encontrados exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    public ResponseEntity<List<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>>> buscarPorEstado( // <-- Changed return type
                                                                                                  @Parameter(description = "Estado del paquete", example = "DISPONIBLE")
                                                                                                  @PathVariable Paquete.Estado estado) {

        Long userId = getUserIdFromToken();
        log.info("Buscando paquetes por estado '{}' para usuario {}", estado, userId);

        // Now the service returns List<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>>
        List<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>> paquetes = paqueteService.buscarPorEstadoLocalizado(estado, userId);
        return ResponseEntity.ok(paquetes); // <-- Return the entire List
    }

    @GetMapping("/disponibles")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    @Operation(summary = "Obtener solo paquetes disponibles localizados")
    public ResponseEntity<List<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>>> obtenerPaquetesDisponibles() { // <-- Changed return type
        Long userId = getUserIdFromToken();
        log.info("Obteniendo paquetes disponibles para usuario {}", userId);

        List<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>> paquetes = paqueteService.buscarPorEstadoLocalizado(Paquete.Estado.DISPONIBLE, userId);
        return ResponseEntity.ok(paquetes); // <-- Return the entire List
    }

    @GetMapping("/{id}/localizacion")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    @Operation(summary = "Obtener estadísticas de localización de un paquete")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
            @ApiResponse(responseCode = "404", description = "Paquete no encontrado"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasLocalizacion(
            @Parameter(description = "ID del paquete", example = "1")
            @PathVariable Long id) {

        Long userId = getUserIdFromToken();
        log.info("Obteniendo estadísticas de localización del paquete {} para usuario {}", id, userId);

        Map<String, Object> estadisticas = paqueteService.getEstadisticasLocalizacion(id, userId);
        return ResponseEntity.ok(estadisticas);
    }

    @GetMapping("/original/{id}")
    @PermitRoles({"ADMIN"})
    @Operation(summary = "Obtener paquete original sin localización (Admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paquete original obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Paquete no encontrado"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN")
    })
    public ResponseEntity<PaqueteDTO> obtenerPaqueteOriginal(
            @Parameter(description = "ID del paquete", example = "1")
            @PathVariable Long id) {

        log.info("Obteniendo paquete original {} (sin localización)", id);

        PaqueteDTO paquete = paqueteService.getPaqueteOriginal(id);
        return ResponseEntity.ok(paquete);
    }

    @GetMapping("/pageable")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    @Operation(summary = "Listar paquetes paginados localizados")
    public ResponseEntity<Page<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>>> listPageLocalizado( // <-- Changed return type
                                                                                                     @Parameter(description = "Número de página", example = "0")
                                                                                                     @RequestParam(defaultValue = "0") int page,
                                                                                                     @Parameter(description = "Tamaño de página", example = "10")
                                                                                                     @RequestParam(defaultValue = "10") int size) {

        Long userId = getUserIdFromToken();
        Pageable pageable = PageRequest.of(page, size);

        // Now the service returns Page<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>>
        Page<LocalizedResponseDto<PaqueteDTO.PaqueteListDTO>> paquetesPage = paqueteService.getPaquetesLocalizados(userId, pageable);
        return ResponseEntity.ok(paquetesPage); // <-- Return the entire Page
    }

    // MÉTODO DE UTILIDAD PARA EXTRAER USER ID DEL TOKEN JWT

    /**
     * Extrae el ID del usuario del token JWT
     * NOTA: Adaptar según tu implementación específica de JWT
     */
    private Long getUserIdFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException("Usuario no autenticado.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getIdUsuario();
        } else {
            log.error("El objeto principal de autenticación no es CustomUserDetails para el usuario '{}'.", authentication.getName());
            throw new IllegalStateException("No se pudo obtener el ID del usuario del contexto de seguridad.");
        }
    }
}