package org.example.granturismo.repositorio;

import org.example.granturismo.modelo.ServicioAlimentacion;

public interface IServicioAlimentacionRepository extends ICrudGenericoRepository<ServicioAlimentacion, Long> {
    ServicioAlimentacion findServicioAlimentacionByServicio_IdServicio(Long servicioIdServicio);
}
