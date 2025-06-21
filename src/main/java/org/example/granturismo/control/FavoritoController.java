package org.example.granturismo.control;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.FavoritoDTO;
import org.example.granturismo.mappers.FavoritoMapper;
import org.example.granturismo.modelo.Favorito;
import org.example.granturismo.security.PermitRoles;
import org.example.granturismo.servicio.IFavoritoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/favoritos")
public class FavoritoController {
    private final IFavoritoService favoritoService;
    private final FavoritoMapper favoritoMapper;

    @GetMapping
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<List<FavoritoDTO>> findAll() {
        List<FavoritoDTO> list = favoritoMapper.toDTOs(favoritoService.findAll());
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<FavoritoDTO> findById(@PathVariable("id") Long id) {
        Favorito obj = favoritoService.findById(id);
        return ResponseEntity.ok(favoritoMapper.toDTO(obj));
    }
    @GetMapping("/buscar-por-tipo")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<List<FavoritoDTO>> findByTipo(
            @RequestParam Long usuarioId,
            @RequestParam String tipo
    ) {
        List<Favorito> lista = favoritoService.findByUsuarioAndTipo(usuarioId, tipo);
        List<FavoritoDTO> dtos = favoritoMapper.toDTOs(lista);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<Void> save(@Valid @RequestBody FavoritoDTO.FavoritoCADTO dto) {
        FavoritoDTO obj = favoritoService.saveD(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdFavorito()).toUri();
        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/{id}")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        favoritoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pageable")
    @PermitRoles({"ADMIN", "USER", "PROV"})
    public ResponseEntity<org.springframework.data.domain.Page<FavoritoDTO>> listPage(Pageable pageable){
        Page<FavoritoDTO> page = favoritoService.listaPage(pageable).map(e -> favoritoMapper.toDTO(e));
        return ResponseEntity.ok(page);
    }
}
