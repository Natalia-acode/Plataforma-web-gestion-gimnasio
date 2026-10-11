package com.gimnasio.service;

import com.gimnasio.domain.Clase;
import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.Zona;
import com.gimnasio.domain.enums.DiaSemana;
import com.gimnasio.domain.enums.EstadoReserva;
import com.gimnasio.domain.enums.Rol;
import com.gimnasio.dto.ClaseRequest;
import com.gimnasio.exception.BusinessException;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.ClaseRepository;
import com.gimnasio.repository.ReservaClaseRepository;
import com.gimnasio.repository.UsuarioRepository;
import com.gimnasio.repository.ZonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

/** CU-21 gestión de clases grupales. */
@Service
public class ClaseService {

    private final ClaseRepository claseRepository;
    private final UsuarioRepository usuarioRepository;
    private final ZonaRepository zonaRepository;
    private final ReservaClaseRepository reservaClaseRepository;

    public ClaseService(ClaseRepository claseRepository, UsuarioRepository usuarioRepository,
                        ZonaRepository zonaRepository, ReservaClaseRepository reservaClaseRepository) {
        this.claseRepository = claseRepository;
        this.usuarioRepository = usuarioRepository;
        this.zonaRepository = zonaRepository;
        this.reservaClaseRepository = reservaClaseRepository;
    }

    public List<Clase> listar(String diaSemana) {
        if (diaSemana == null || diaSemana.isBlank()) {
            return claseRepository.findByActivaTrue();
        }
        return claseRepository.findByActivaTrueAndDiaSemana(DiaSemana.desdeValor(diaSemana.toLowerCase()));
    }

    public Clase obtener(Long id) {
        return claseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clase no encontrada: " + id));
    }

    public long reservasConfirmadas(Clase clase, java.time.LocalDate fecha) {
        return reservaClaseRepository.countByClaseIdAndFechaAndEstado(clase.getId(), fecha, EstadoReserva.CONFIRMADA);
    }

    @Transactional
    public Clase crear(ClaseRequest solicitud) {
        Usuario entrenador = usuarioRepository.findById(solicitud.idEntrenador())
                .orElseThrow(() -> new NotFoundException("Entrenador no encontrado: " + solicitud.idEntrenador()));
        if (entrenador.getRol() != Rol.ENTRENADOR && entrenador.getRol() != Rol.ADMINISTRADOR) {
            throw new BusinessException("El usuario seleccionado no tiene rol de entrenador");
        }
        Zona zona = zonaRepository.findById(solicitud.idZona())
                .orElseThrow(() -> new NotFoundException("Zona no encontrada: " + solicitud.idZona()));

        LocalTime inicio = LocalTime.parse(solicitud.horaInicio());
        LocalTime fin = LocalTime.parse(solicitud.horaFin());
        if (!fin.isAfter(inicio)) {
            throw new BusinessException("La hora de fin debe ser posterior a la hora de inicio");
        }
        if (solicitud.cupoMaximo() > zona.getCapacidadMaxima()) {
            throw new BusinessException("El cupo no puede superar la capacidad de la zona ("
                    + zona.getCapacidadMaxima() + ")");
        }

        Clase clase = new Clase();
        clase.setNombre(solicitud.nombre());
        clase.setDiaSemana(DiaSemana.desdeValor(solicitud.diaSemana().toLowerCase()));
        clase.setHoraInicio(inicio);
        clase.setHoraFin(fin);
        clase.setCupoMaximo(solicitud.cupoMaximo());
        clase.setEntrenador(entrenador);
        clase.setZona(zona);
        return claseRepository.save(clase);
    }

    @Transactional
    public Clase desactivar(Long id) {
        Clase clase = obtener(id);
        clase.setActiva(false);
        return claseRepository.save(clase);
    }
}
