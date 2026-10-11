package com.gimnasio.service;

import com.gimnasio.domain.TurnoCola;
import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.enums.EstadoTurno;
import com.gimnasio.domain.enums.TipoNotificacion;
import com.gimnasio.domain.enums.TipoTurno;
import com.gimnasio.exception.BusinessException;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.TurnoColaRepository;
import com.gimnasio.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** CU-22 solicitar turno, CU-23 posición/espera, CU-24 cancelar, CU-25 operación de la cola (RN-08). */
@Service
public class ColaService {

    private static final List<EstadoTurno> ACTIVOS = List.of(EstadoTurno.ESPERANDO, EstadoTurno.LLAMADO);

    private final TurnoColaRepository turnoColaRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;
    private final ConfiguracionService configuracionService;

    public ColaService(TurnoColaRepository turnoColaRepository, UsuarioRepository usuarioRepository,
                       NotificacionService notificacionService, ConfiguracionService configuracionService) {
        this.turnoColaRepository = turnoColaRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionService = notificacionService;
        this.configuracionService = configuracionService;
    }

    /** CU-22: solicitar turno (solo uno activo por usuario; EX de CU-22). */
    @Transactional
    public TurnoCola solicitar(Long usuarioId, String tipo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + usuarioId));
        if (turnoColaRepository.findFirstByUsuarioIdAndEstadoIn(usuarioId, ACTIVOS).isPresent()) {
            throw new BusinessException("Ya tiene un turno activo en la cola virtual");
        }
        long enEspera = turnoColaRepository.countByEstadoIn(ACTIVOS);
        TurnoCola turno = new TurnoCola();
        turno.setUsuario(usuario);
        turno.setTipo(TipoTurno.desdeValor(tipo.toLowerCase()));
        turno.setFechaSolicitud(LocalDateTime.now());
        turno.setPosicion((int) enEspera + 1);
        turno.setEstado(EstadoTurno.ESPERANDO);
        turnoColaRepository.save(turno);
        notificacionService.enviar(usuario, TipoNotificacion.TURNO_PROXIMO,
                String.format("Turno virtual registrado (%s). Su posición es la %d.", tipo, turno.getPosicion()));
        return turno;
    }

    /** CU-23: consulta del turno propio con personas delante y espera estimada. */
    public TurnoCola miTurno(Long usuarioId) {
        return turnoColaRepository.findFirstByUsuarioIdAndEstadoIn(usuarioId, ACTIVOS).orElse(null);
    }

    public long personasDelante(TurnoCola turno) {
        return turnoColaRepository.findByEstadoInOrderByFechaSolicitudAsc(ACTIVOS).stream()
                .filter(t -> t.getFechaSolicitud().isBefore(turno.getFechaSolicitud()))
                .count();
    }

    public int esperaEstimadaMin(TurnoCola turno) {
        int rotacion = configuracionService.obtener().getTiempoRotacionMin();
        return (int) personasDelante(turno) * rotacion;
    }

    /** CU-24: cancelar el turno propio. */
    @Transactional
    public TurnoCola cancelar(Long usuarioId) {
        TurnoCola turno = miTurno(usuarioId);
        if (turno == null) {
            throw new BusinessException("No tiene un turno activo que cancelar");
        }
        turno.setEstado(EstadoTurno.CANCELADO);
        turnoColaRepository.save(turno);
        reordenar();
        return turno;
    }

    /** CU-25: vista de recepción con la cola completa. */
    public List<TurnoCola> colaCompleta() {
        return turnoColaRepository.findByEstadoInOrderByFechaSolicitudAsc(ACTIVOS);
    }

    /** CU-25: llamar al siguiente de la fila. */
    @Transactional
    public TurnoCola llamarSiguiente() {
        TurnoCola siguiente = turnoColaRepository.findByEstadoInOrderByFechaSolicitudAsc(ACTIVOS).stream()
                .filter(t -> t.getEstado() == EstadoTurno.ESPERANDO)
                .findFirst()
                .orElseThrow(() -> new BusinessException("No hay personas esperando en la cola"));
        siguiente.setEstado(EstadoTurno.LLAMADO);
        siguiente.setHoraLLamado(LocalDateTime.now());
        turnoColaRepository.save(siguiente);

        int minutos = configuracionService.obtener().getVentanaTurnoMin();
        notificacionService.enviar(siguiente.getUsuario(), TipoNotificacion.TURNO_PROXIMO,
                String.format("¡Es su turno! Preséntese en recepción dentro de los próximos %d minutos.", minutos));
        return siguiente;
    }

    /** CU-25: recepción marca el turno como atendido. */
    @Transactional
    public TurnoCola marcarAtendido(Long turnoId) {
        TurnoCola turno = turnoColaRepository.findById(turnoId)
                .orElseThrow(() -> new NotFoundException("Turno no encontrado: " + turnoId));
        if (turno.getEstado() != EstadoTurno.LLAMADO) {
            throw new BusinessException("Solo se pueden atender turnos previamente llamados");
        }
        turno.setEstado(EstadoTurno.ATENDIDO);
        turno.setHoraAtencion(LocalDateTime.now());
        turnoColaRepository.save(turno);
        reordenar();
        return turno;
    }

    /** RN-08: expira los turnos llamados que no se presentaron dentro de la ventana. */
    @Transactional
    public int expirarTurnosVencidos() {
        int minutos = configuracionService.obtener().getVentanaTurnoMin();
        int expirados = turnoColaRepository.expirarTurnos(EstadoTurno.LLAMADO, EstadoTurno.EXPIRADO,
                LocalDateTime.now().minusMinutes(minutos));
        if (expirados > 0) {
            reordenar();
        }
        return expirados;
    }

    private void reordenar() {
        List<TurnoCola> activos = turnoColaRepository.findByEstadoInOrderByFechaSolicitudAsc(ACTIVOS);
        int posicion = 1;
        for (TurnoCola turno : activos) {
            if (turno.getPosicion() != posicion) {
                turno.setPosicion(posicion);
                turnoColaRepository.save(turno);
            }
            posicion++;
        }
    }
}
