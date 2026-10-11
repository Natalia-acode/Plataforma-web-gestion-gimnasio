package com.gimnasio.service;

import com.gimnasio.domain.CheckIn;
import com.gimnasio.domain.Suscripcion;
import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.enums.EstadoSuscripcion;
import com.gimnasio.domain.enums.MetodoCheckIn;
import com.gimnasio.exception.BusinessException;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.CheckInRepository;
import com.gimnasio.repository.SuscripcionRepository;
import com.gimnasio.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** CU-13 check-in, CU-14 registro de salida, CU-15 base de ocupación. */
@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final UsuarioRepository usuarioRepository;
    private final SuscripcionRepository suscripcionRepository;

    public CheckInService(CheckInRepository checkInRepository, UsuarioRepository usuarioRepository,
                          SuscripcionRepository suscripcionRepository) {
        this.checkInRepository = checkInRepository;
        this.usuarioRepository = usuarioRepository;
        this.suscripcionRepository = suscripcionRepository;
    }

    /** Valida suscripción activa y vigente antes de permitir el ingreso. */
    public void validarAcceso(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + usuarioId));
        if (usuario.getEstado() == com.gimnasio.domain.enums.EstadoUsuario.SUSPENDIDO) {
            throw new BusinessException("La cuenta está suspendida");
        }
        Suscripcion suscripcion = suscripcionRepository
                .findFirstByUsuarioIdAndEstado(usuarioId, EstadoSuscripcion.ACTIVA)
                .orElseThrow(() -> new BusinessException("No tiene una suscripción activa"));
        if (suscripcion.getFechaFin().isBefore(LocalDate.now())) {
            throw new BusinessException("Su suscripción venció el " + suscripcion.getFechaFin());
        }
        if (checkInRepository.findFirstByUsuarioIdAndFechaHoraSalidaIsNull(usuarioId).isPresent()) {
            throw new BusinessException("Ya registra un ingreso sin salida; registre primero su salida");
        }
    }

    /** CU-13: entrada con QR (sin recepcionista asociado). */
    @Transactional
    public CheckIn registrarEntradaQr(Long usuarioId) {
        validarAcceso(usuarioId);
        return crearEntrada(usuarioId, null, MetodoCheckIn.QR);
    }

    /** CU-13 (alternativo): entrada manual registrada por recepción. */
    @Transactional
    public CheckIn registrarEntradaManual(Long usuarioId, Long recepcionistaId) {
        validarAcceso(usuarioId);
        return crearEntrada(usuarioId, recepcionistaId, MetodoCheckIn.MANUAL);
    }

    /** CU-14: salida; cierra la entrada abierta del usuario. */
    @Transactional
    public CheckIn registrarSalida(Long usuarioId) {
        CheckIn checkIn = checkInRepository.findFirstByUsuarioIdAndFechaHoraSalidaIsNull(usuarioId)
                .orElseThrow(() -> new BusinessException("El usuario no tiene un ingreso abierto"));
        checkIn.setFechaHoraSalida(LocalDateTime.now());
        return checkInRepository.save(checkIn);
    }

    public List<CheckIn> dentroDelGimnasio() {
        return checkInRepository.findByFechaHoraSalidaIsNullAndFechaHoraEntradaBefore(LocalDateTime.now());
    }

    public long personasDentro() {
        return checkInRepository.countByFechaHoraSalidaIsNull();
    }

    public List<CheckIn> ingresosDelDia() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        return checkInRepository.findByFechaHoraEntradaBetween(inicio, inicio.plusDays(1));
    }

    /** RN-11: cierre automático de registros olvidados para no inflar la ocupación. */
    @Transactional
    public int cerrarRegistrosInactivos(int horasMaximas) {
        List<CheckIn> abiertosViejos = checkInRepository
                .findByFechaHoraSalidaIsNullAndFechaHoraEntradaBefore(LocalDateTime.now().minusHours(horasMaximas));
        for (CheckIn checkIn : abiertosViejos) {
            checkIn.setFechaHoraSalida(checkIn.getFechaHoraEntrada().plusHours(horasMaximas));
            checkInRepository.save(checkIn);
        }
        return abiertosViejos.size();
    }

    private CheckIn crearEntrada(Long usuarioId, Long recepcionistaId, MetodoCheckIn metodo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + usuarioId));
        CheckIn checkIn = new CheckIn();
        checkIn.setUsuario(usuario);
        if (recepcionistaId != null) {
            checkIn.setRecepcionista(usuarioRepository.findById(recepcionistaId)
                    .orElseThrow(() -> new NotFoundException("Recepcionista no encontrado: " + recepcionistaId)));
        }
        checkIn.setFechaHoraEntrada(LocalDateTime.now());
        checkIn.setMetodo(metodo);
        return checkInRepository.save(checkIn);
    }
}
