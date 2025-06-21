package org.example.granturismo.servicio;

import org.example.granturismo.dtos.FavoritoDTO;
import org.example.granturismo.modelo.Favorito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IFavoritoService extends ICrudGenericoService<Favorito, Long> {

    FavoritoDTO saveD(FavoritoDTO.FavoritoCADTO dto);

    FavoritoDTO updateD(FavoritoDTO.FavoritoCADTO dto, Long id);

    List<Favorito> findByUsuarioAndTipo(Long usuarioId, String tipo);

    Page<Favorito> listaPage(Pageable pageable);
}
