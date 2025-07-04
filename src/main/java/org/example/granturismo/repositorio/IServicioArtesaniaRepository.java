package org.example.granturismo.repositorio;

import org.example.granturismo.modelo.ServicioArtesania;

public interface IServicioArtesaniaRepository extends ICrudGenericoRepository<ServicioArtesania, Long> {
    ServicioArtesania findServicioArtesaniaByServicio_IdServicio(Long servicioIdServicio);
}
