package org.example.granturismo.servicio;


import org.example.granturismo.dtos.ReservaDTO;
import org.example.granturismo.modelo.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IReservaService extends ICrudGenericoService<Reserva, Long>{

    ReservaDTO saveD(ReservaDTO.ReservaCADTO dto);

    ReservaDTO updateD(ReservaDTO.ReservaCADTO dto, Long id);

    Page<Reserva> listaPage(Pageable pageable);
}
