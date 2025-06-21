package org.example.granturismo.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.granturismo.dtos.FavoritoDTO;
import org.example.granturismo.mappers.FavoritoMapper;
import org.example.granturismo.modelo.Favorito;
import org.example.granturismo.modelo.Proveedor;
import org.example.granturismo.modelo.Usuario;
import org.example.granturismo.repositorio.ICrudGenericoRepository;
import org.example.granturismo.repositorio.IFavoritoRepository;
import org.example.granturismo.repositorio.IUsuarioRepository;
import org.example.granturismo.servicio.IFavoritoService;
import org.example.granturismo.servicio.IProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FavoritoServiceImp extends CrudGenericoServiceImp <Favorito, Long> implements IFavoritoService {
    @Autowired
    private DataSource dataSource;

    private final IFavoritoRepository repo;
    private final FavoritoMapper favoritoMapper;
    private final IUsuarioRepository usuarioRepository;

    @Override
    protected ICrudGenericoRepository<Favorito, Long> getRepo() {
        return repo;
    }

    @Override
    public FavoritoDTO saveD(FavoritoDTO.FavoritoCADTO dto) {
        Favorito favorito = favoritoMapper.toEntityFromCADTO(dto);

        Usuario usuario = usuarioRepository.findById(dto.usuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        favorito.setUsuario(usuario);
        Favorito favoritoGuardado = repo.save(favorito);
        return favoritoMapper.toDTO(favoritoGuardado);
    }

    @Override
    public FavoritoDTO updateD(FavoritoDTO.FavoritoCADTO dto, Long id) {
        Favorito favorito = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Favorito no encontrado"));

        Favorito favoritox = favoritoMapper.toEntityFromCADTO(dto);
        favoritox.setIdFavorito(favorito.getIdFavorito());

        Usuario usuario = usuarioRepository.findById(dto.usuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        favoritox.setUsuario(usuario);

        Favorito favoritoActualizado = repo.save(favoritox);
        return favoritoMapper.toDTO(favoritoActualizado);
    }

    @Override
    public List<Favorito> findByUsuarioAndTipo(Long usuarioId, String tipo) {
        return repo.findByUsuario_IdUsuarioAndTipo(usuarioId, tipo);
    }

    public Page<Favorito> listaPage(Pageable pageable){
        return repo.findAll(pageable);
    }
}
