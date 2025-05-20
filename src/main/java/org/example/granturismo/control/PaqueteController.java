package org.example.granturismo.control;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.PaqueteDTO;
import org.example.granturismo.mappers.PaqueteMapper;
import org.example.granturismo.modelo.Paquete;
import org.example.granturismo.security.PermitRoles;
import org.example.granturismo.servicio.IPaqueteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class PaqueteController {


    private final IPaqueteService paqueteService;
    private final PaqueteMapper paqueteMapper;

    @GetMapping
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<List<PaqueteDTO>> findAll() {
        List<Paquete> paquetes = paqueteService.findAll();
        List<PaqueteDTO> list = paqueteMapper.toDTOs(paquetes);
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<PaqueteDTO> findById(@PathVariable("id") Long id) {
        Paquete obj = paqueteService.findById(id);
        return ResponseEntity.ok(paqueteMapper.toDTO(obj));
    }

    @PostMapping // Espera multipart/form-data
    @PermitRoles({"ADMIN"})
    public ResponseEntity<?> save(
            @Valid @RequestPart("dto") PaqueteDTO.PaqueteCADTO dto,
            @RequestPart("imagenFile") MultipartFile imagenFile // Ahora se espera el archivo aquí
    ) {
        try {
            // Llama al servicio pasando el DTO y el archivo
            PaqueteDTO obj = paqueteService.saveD(dto, imagenFile);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdPaquete()).toUri();
            return ResponseEntity.created(location).body(obj);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error de E/S al procesar la imagen: " + e.getMessage()));
        } catch (IllegalArgumentException e) { // Captura si la imagen obligatoria falta
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ocurrió un error al crear el paquete: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}") // Espera multipart/form-data
    @PermitRoles({"ADMIN", "PROV"})
    public ResponseEntity<?> update(
            @PathVariable("id") Long id,
            @Valid @RequestPart("dto") PaqueteDTO.PaqueteCADTO dto,
            @RequestPart(value = "imagenFile", required = false) MultipartFile imagenFile // Archivo de imagen (opcional al actualizar)
    ) {
        try {
            // Llama al servicio pasando el ID, el DTO y el archivo (que puede ser null)
            PaqueteDTO obj = paqueteService.updateD(dto, id, imagenFile);
            return ResponseEntity.ok(obj);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error de E/S al procesar la imagen al actualizar: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ocurrió un error al actualizar el paquete: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PermitRoles({"ADMIN"})
    public ResponseEntity<?> delete(@PathVariable("id") Long id) { // Cambiado a ResponseEntity<?> para poder devolver un cuerpo de error si es necesario
        try {
            paqueteService.delete(id);
            return ResponseEntity.noContent().build(); // 204 No Content si tiene éxito
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage())); // 404 si no existe
        } catch (Exception e) { // Captura cualquier otro error, incluyendo de Cloudinary si se relanza en el servicio
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al eliminar el paquete: " + e.getMessage())); // 500
        }
    }

    @GetMapping("/pageable")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<org.springframework.data.domain.Page<PaqueteDTO>> listPage(Pageable pageable){
        Page<PaqueteDTO> page = paqueteService.listaPage(pageable).map(e -> paqueteMapper.toDTO(e));
        return ResponseEntity.ok(page);
    }
}
