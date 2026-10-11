package com.gimnasio.service;

import com.gimnasio.domain.Clase;
import com.gimnasio.domain.ReservaClase;
import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.enums.EstadoReserva;
import com.gimnasio.domain.enums.TipoNotificacion;
import com.gimnasio.exception.BusinessException;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.ReservaClaseRepository;
import com.gimnasio.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** CU-18 reservar, CU-19 cancelar reserva, CU-20 lista de espera (RN-09). */
@Service
public class ReservaService {

    private static final List<EstadoReserva> VIGENTES = List.of(EstadoReserva.CONFIRMADA, EstadoReserva.EN_ESPERA);

    private final ReservaClaseRepository reservaClaseRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClaseService claseService;
    private final NotificacionService notificacionService;

    public ReservaService(ReservaClaseRepository reservaClaseRepository, UsuarioRepository usuarioRepository,
                          ClaseService claseService, NotificacionService notificacionService) {
        this.reservaClaseRepository = reservaClaseRepository;
        this.usuarioRepository = usuarioRepository;
        this.claseService = claseService;
        this.notificacionService = notificacionService;
    }

    public List<ReservaClase> listarDe(Long usuarioId) {
        return reservaClaseRepository.findByUsuarioIdAndEstadoInOrderByFechaDesc(usuarioId, VIGENTES);
    }

    public List<ReservaClase> asistentes(Long claseId, LocalDate fecha) {
        return reservaClaseRepository.findByClaseIdAndFechaAndEstadoInOrderByFechaReservaAsc(
                claseId, fecha, List.of(EstadoReserva.CONFIRMADA, EstadoReserva.ASISTIO, EstadoReserva.NO_ASISTIO));
    }

    /**
     * CU-18: reserva con cupo; si está llena, ingresa a lista de espera con posición (RN-09).
     */
    @Transactional
    public ReservaClase reservar(Long usuarioId, Long claseId, LocalDate fecha) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + usuarioId));
        Clase clase = claseService.obtener(claseId);
        if (!clase.isActiva()) {
            throw new BusinessException("La clase no está disponible");
        }
        if (!fecha.plusDays(1).isAfter(LocalDate.now())) {
            throw new BusinessException("Solo se pueden reservar clases de hoy en adelante");
        }

        // EX-2 de CU-18: sin reservas vigentes duplicadas para la misma sesión.
        if (reservaClaseRepository.findFirstByClaseIdAndUsuarioIdAndFechaAndEstadoIn(
                claseId, usuarioId, fecha, VIGENTES).isPresent()) {
            throw new BusinessException("Ya tiene una reserva vigente para esta clase en esa fecha");
        }

        long confirmadas = claseService.reservasConfirmadas(clase, fecha);
        ReservaClase reserva = new ReservaClase();
        reserva.setClase(clase);
        reserva.setUsuario(usuario);
        reserva.setFecha(fecha);

        if (confirmadas < clase.getCupoMaximo()) {
            reserva.setEstado(EstadoReserva.CONFIRMADA);
            reservaClaseRepository.save(reserva);
            notificacionService.enviar(usuario, TipoNotificacion.CONFIRMACION_RESERVA,
                    String.format("Reserva confirmada: %s el %s a las %s.",
                            clase.getNombre(), fecha, clase.getHoraInicio()));
        } else {
            int posicion = reservaClaseRepository
                    .findByClaseIdAndFechaAndEstadoOrderByPosicionEsperaAsc(claseId, fecha, EstadoReserva.EN_ESPERA)
                    .size() + 1;
            reserva.setEstado(EstadoReserva.EN_ESPERA);
            reserva.setPosicionEspera(posicion);
            reservaClaseRepository.save(reserva);
            notificacionService.enviar(usuario, TipoNotificacion.CONFIRMACION_RESERVA,
                    String.format("La clase %s está llena. Quedó en lista de espera, posición %d.",
                            clase.getNombre(), posicion));
        }
        return reserva;
    }

    /**
     * CU-19: cancelación. Si el cupo queda libre, se promueve al primero de la lista de espera.
     */
    @Transactional
    public ReservaClase cancelar(Long usuarioId, Long reservaId) {
        ReservaClase reserva = reservaClaseRepository.findById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada: " + reservaId));
        if (!reserva.getUsuario().getId().equals(usuarioId)) {
            throw new NotFoundException("Reserva no encontrada: " + reservaId);
        }
        if (!reserva.estaVigente()) {
            throw new BusinessException("La reserva no está vigente");
        }
        boolean ocupabaCupo = reserva.getEstado() == EstadoReserva.CONFIRMADA;
        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setPosicionEspera(null);
        reservaClaseRepository.save(reserva);

        if (ocupabaCupo) {
            promoverListaEspera(reserva.getClase(), reserva.getFecha());
        } else {
            reordenarListaEspera(reserva.getClase(), reserva.getFecha());
        }
        return reserva;
    }

    /** CU-20: el entrenador marca asistencia; libera cupo y promueve al siguiente en espera. */
    @Transactional
    public ReservaClase marcarAsistencia(Long reservaId, boolean asistio) {
        ReservaClase reserva = reservaClaseRepository.findById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada: " + reservaId));
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new BusinessException("Solo se marca asistencia de reservas confirmadas");
        }
        reserva.setEstado(asistio ? EstadoReserva.ASISTIO : EstadoReserva.NO_ASISTIO);
        reservaClaseRepository.save(reserva);

        if (!asistio) {
            promoverListaEspera(reserva.getClase(), reserva.getFecha());
        }
        return reserva;
    }

    private void promoverListaEspera(Clase clase, LocalDate fecha) {
        List<ReservaClase> espera = reservaClaseRepository
                .findByClaseIdAndFechaAndEstadoOrderByPosicionEsperaAsc(clase.getId(), fecha, EstadoReserva.EN_ESPERA);
        if (!espera.isEmpty()) {
            ReservaClase siguiente = espera.get(0);
            siguiente.setEstado(EstadoReserva.CONFIRMADA);
            siguiente.setPosicionEspera(null);
            reservaClaseRepository.save(siguiente);
            notificacionService.enviar(siguiente.getUsuario(), TipoNotificacion.CONFIRMACION_RESERVA,
                    String.format("¡Se liberó un cupo! Su reserva para %s el %s quedó confirmada.",
                            clase.getNombre(), fecha));
        }
        reordenarListaEspera(clase, fecha);
    }

    private void reordenarListaEspera(Clase clase, LocalDate fecha) {
        List<ReservaClase> espera = reservaClaseRepository
                .findByClaseIdAndFechaAndEstadoOrderByPosicionEsperaAsc(clase.getId(), fecha, EstadoReserva.EN_ESPERA);
        int posicion = 1;
        for (ReservaClase reserva : espera) {
            if (reserva.getPosicionEspera() == null || reserva.getPosicionEspera() != posicion) {
                reserva.setPosicionEspera(posicion);
                reservaClaseRepository.save(reserva);
            }
            posicion++;
        }
    }
}
