package org.example.granturismo.repositorio;

import org.example.granturismo.modelo.Favorito;

import java.util.List;

public interface IFavoritoRepository extends ICrudGenericoRepository<Favorito, Long>{
    List<Favorito> findByUsuario_IdUsuarioAndTipo(Long usuarioId, String tipo);
}
