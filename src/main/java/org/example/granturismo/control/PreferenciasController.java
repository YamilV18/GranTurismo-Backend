package org.example.granturismo.control;
/*
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.PreferenciasDTO;
import org.example.granturismo.mappers.PreferenciasMapper;
import org.example.granturismo.security.PermitRoles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/preferencias")
public class PreferenciasController {


    private final IPreferenciasService preferenciasService;
    private final PreferenciasMapper preferenciasMapper;

    @GetMapping("/convertir")
    @PermitRoles({"USER", "PROV", "ADMIN"})
    public ResponseEntity<BigDecimal> convertirMoneda(
            @RequestParam BigDecimal monto,
            @RequestParam Long idUsuario
    ) {
        BigDecimal resultado = preferenciasService.convertirMoneda(monto, idUsuario);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<List<PreferenciasDTO>> findAll() {
        List<PreferenciasDTO> list = preferenciasMapper.toDTOs(preferenciasService.findAll());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<PreferenciasDTO> findById(@PathVariable("id") Long id) {
        Preferencias obj = preferenciasService.findById(id);
        return ResponseEntity.ok(preferenciasMapper.toDTO(obj));
    }

    @PostMapping
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<Void> save(@Valid @RequestBody PreferenciasDTO.PreferenciasCADTO dto) {
        PreferenciasDTO obj = preferenciasService.saveD(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdPreferencia()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<PreferenciasDTO> update(@Valid @RequestBody PreferenciasDTO.PreferenciasCADTO dto, @PathVariable("id") Long id) {
        PreferenciasDTO obj = preferenciasService.updateD(dto, id);
        return ResponseEntity.ok(obj);
    }

    @DeleteMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        preferenciasService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pageable")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<org.springframework.data.domain.Page<PreferenciasDTO>> listPage(Pageable pageable){
        Page<PreferenciasDTO> page = preferenciasService.listaPage(pageable).map(e -> preferenciasMapper.toDTO(e));
        return ResponseEntity.ok(page);
    }
}*/
