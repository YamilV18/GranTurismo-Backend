package org.example.granturismo.control;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.ReservaDTO;
import org.example.granturismo.mappers.ReservaMapper;
import org.example.granturismo.modelo.Reserva;
import org.example.granturismo.security.PermitRoles;
import org.example.granturismo.servicio.IReservaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final IReservaService reservaService;
    private final ReservaMapper reservaMapper;

    @GetMapping
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<List<ReservaDTO>> findAll() {
        List<ReservaDTO> list = reservaMapper.toDTOs(reservaService.findAll());
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<ReservaDTO> findById(@PathVariable("id") Long id) {
        Reserva obj = reservaService.findById(id);
        return ResponseEntity.ok(reservaMapper.toDTO(obj));
    }

    @PostMapping
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<Void> save(@Valid @RequestBody ReservaDTO.ReservaCADTO dto) {
        ReservaDTO obj = reservaService.saveD(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdReserva()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<ReservaDTO> update(@Valid @RequestBody ReservaDTO.ReservaCADTO dto, @PathVariable("id") Long id) {
        ReservaDTO obj = reservaService.updateD(dto, id);
        return ResponseEntity.ok(obj);
    }

    @DeleteMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        reservaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pageable")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<org.springframework.data.domain.Page<ReservaDTO>> listPage(Pageable pageable){
        Page<ReservaDTO> page = reservaService.listaPage(pageable).map(e -> reservaMapper.toDTO(e));
        return ResponseEntity.ok(page);
    }
}
