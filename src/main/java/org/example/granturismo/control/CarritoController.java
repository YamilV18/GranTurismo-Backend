package org.example.granturismo.control;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.CarritoDTO;
import org.example.granturismo.dtos.FavoritoDTO;
import org.example.granturismo.dtos.ReservaDTO;
import org.example.granturismo.mappers.CarritoMapper;
import org.example.granturismo.mappers.ReservaMapper;
import org.example.granturismo.modelo.Carrito;
import org.example.granturismo.modelo.Favorito;
import org.example.granturismo.modelo.Reserva;
import org.example.granturismo.security.PermitRoles;
import org.example.granturismo.servicio.ICarritoService;
import org.example.granturismo.servicio.IReservaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/carrito")
public class CarritoController {

    private final ICarritoService carritoService;
    private final CarritoMapper carritoMapper;

    @GetMapping
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<List<CarritoDTO>> findAll() {
        List<CarritoDTO> list = carritoMapper.toDTOs(carritoService.findAll());
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<CarritoDTO> findById(@PathVariable("id") Long id) {
        Carrito obj = carritoService.findById(id);
        return ResponseEntity.ok(carritoMapper.toDTO(obj));
    }
    @GetMapping("/buscar-por-usuario/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<CarritoDTO> findByTipo(
            @PathVariable("id") Long usuarioId
    ) {
        Optional<Carrito> car = carritoService.findByUsuario(usuarioId);
        CarritoDTO dto = carritoMapper.toDTO(car.get());
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<Void> save(@Valid @RequestBody CarritoDTO.CarritoCADTO dto) {
        CarritoDTO obj = carritoService.saveD(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdCarrito()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<CarritoDTO> update(@Valid @RequestBody CarritoDTO.CarritoCADTO dto, @PathVariable("id") Long id) {
        CarritoDTO obj = carritoService.updateD(dto, id);
        return ResponseEntity.ok(obj);
    }

    @DeleteMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        carritoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pageable")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<org.springframework.data.domain.Page<CarritoDTO>> listPage(Pageable pageable){
        Page<CarritoDTO> page = carritoService.listaPage(pageable).map(e -> carritoMapper.toDTO(e));
        return ResponseEntity.ok(page);
    }
}
