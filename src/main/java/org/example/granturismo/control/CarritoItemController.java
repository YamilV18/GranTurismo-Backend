package org.example.granturismo.control;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.CarritoDTO;
import org.example.granturismo.dtos.CarritoItemDTO;
import org.example.granturismo.dtos.FavoritoDTO;
import org.example.granturismo.mappers.CarritoItemMapper;
import org.example.granturismo.mappers.CarritoMapper;
import org.example.granturismo.modelo.Carrito;
import org.example.granturismo.modelo.CarritoItem;
import org.example.granturismo.modelo.Favorito;
import org.example.granturismo.security.PermitRoles;
import org.example.granturismo.servicio.ICarritoItemService;
import org.example.granturismo.servicio.ICarritoService;
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
@RequestMapping("/carritoitem")
public class CarritoItemController {

    private final ICarritoItemService carritoItemService;
    private final CarritoItemMapper carritoItemMapper;

    @GetMapping
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<List<CarritoItemDTO>> findAll() {
        List<CarritoItemDTO> list = carritoItemMapper.toDTOs(carritoItemService.findAll());
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<CarritoItemDTO> findById(@PathVariable("id") Long id) {
        CarritoItem obj = carritoItemService.findById(id);
        return ResponseEntity.ok(carritoItemMapper.toDTO(obj));
    }

    @GetMapping("/buscar-por-tipo")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<CarritoItemDTO> findByTipo(
            @RequestParam Long carritoId,
            @RequestParam Long referenciaId,
            @RequestParam String tipo
    ) {
        Optional<CarritoItem> car = carritoItemService.findByCarritoIdAndReferenciaIdAndTipo(carritoId,tipo,referenciaId);
        CarritoItemDTO dto = carritoItemMapper.toDTO(car.get());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/buscar-por-carrito")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<List<CarritoItemDTO>> findByCarrito(
            @RequestParam Long carritoId
    ) {
        List<CarritoItem> lista = carritoItemService.findByCarrito(carritoId);
        List<CarritoItemDTO> dtos = carritoItemMapper.toDTOs(lista);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<Void> save(@Valid @RequestBody CarritoItemDTO.CarritoItemCADTO dto) {
        CarritoItemDTO obj = carritoItemService.saveD(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdCarritoItem()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<CarritoItemDTO> update(@Valid @RequestBody CarritoItemDTO.CarritoItemCADTO dto, @PathVariable("id") Long id) {
        CarritoItemDTO obj = carritoItemService.updateD(dto, id);
        return ResponseEntity.ok(obj);
    }

    @DeleteMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        carritoItemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pageable")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<org.springframework.data.domain.Page<CarritoItemDTO>> listPage(Pageable pageable){
        Page<CarritoItemDTO> page = carritoItemService.listaPage(pageable).map(e -> carritoItemMapper.toDTO(e));
        return ResponseEntity.ok(page);
    }
}