package org.example.granturismo.control;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.PaqueteDTO;
import org.example.granturismo.dtos.ServicioArtesaniaDTO;
import org.example.granturismo.dtos.ServicioDTO;
import org.example.granturismo.dtos.ServicioHoteleriaDTO;
import org.example.granturismo.mappers.ServicioHoteleriaMapper;
import org.example.granturismo.mappers.ServicioMapper;
import org.example.granturismo.modelo.Paquete;
import org.example.granturismo.modelo.Servicio;
import org.example.granturismo.modelo.ServicioArtesania;
import org.example.granturismo.modelo.ServicioHoteleria;
import org.example.granturismo.security.PermitRoles;
import org.example.granturismo.servicio.IServicioHoteleriaService;
import org.example.granturismo.servicio.IServicioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/serviciohoteles")
public class ServicioHoteleriaController {

    private final IServicioHoteleriaService servicioHoteleriaService;
    private final ServicioHoteleriaMapper servicioHoteleriaMapper;

    @GetMapping
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<List<ServicioHoteleriaDTO>> findAll() {
        List<ServicioHoteleriaDTO> list = servicioHoteleriaMapper.toDTOs(servicioHoteleriaService.findAll());
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<ServicioHoteleriaDTO> findById(@PathVariable("id") Long id) {
        ServicioHoteleria obj = servicioHoteleriaService.findById(id);
        return ResponseEntity.ok(servicioHoteleriaMapper.toDTO(obj));
    }

    @GetMapping("/servicio/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<ServicioHoteleriaDTO> findByServicio(
            @PathVariable("id") Long servicioId
    ) {
        ServicioHoteleria hot = servicioHoteleriaService.findByServicio(servicioId);
        if (hot == null) {
            return ResponseEntity.notFound().build();
        }
        ServicioHoteleriaDTO dto = servicioHoteleriaMapper.toDTO(hot);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PermitRoles({"ADMIN"})
    public ResponseEntity<Void> save(@Valid @RequestBody ServicioHoteleriaDTO.ServicioHoteleriaCADTO dto) {
        ServicioHoteleriaDTO obj = servicioHoteleriaService.saveD(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdHoteleria()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    @PermitRoles({"ADMIN", "PROV"})
    public ResponseEntity<ServicioHoteleriaDTO> update(@Valid @RequestBody ServicioHoteleriaDTO.ServicioHoteleriaCADTO dto, @PathVariable("id") Long id) {
        ServicioHoteleriaDTO obj = servicioHoteleriaService.updateD(dto, id);
        return ResponseEntity.ok(obj);
    }

    @DeleteMapping("/{id}")
    @PermitRoles({"ADMIN"})
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        servicioHoteleriaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pageable")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<org.springframework.data.domain.Page<ServicioHoteleriaDTO>> listPage(Pageable pageable){
        Page<ServicioHoteleriaDTO> page = servicioHoteleriaService.listaPage(pageable).map(e -> servicioHoteleriaMapper.toDTO(e));
        return ResponseEntity.ok(page);
    }
}
