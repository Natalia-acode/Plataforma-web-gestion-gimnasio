package com.gimnasio.service;

import com.gimnasio.domain.MetodoPago;
import com.gimnasio.domain.Plan;
import com.gimnasio.domain.Suscripcion;
import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.enums.EstadoSuscripcion;
import com.gimnasio.domain.enums.TipoNotificacion;
import com.gimnasio.domain.enums.TipoPago;
import com.gimnasio.dto.ContratarSuscripcionRequest;
import com.gimnasio.dto.ResultadoCobro;
import com.gimnasio.dto.VentaPresencialRequest;
import com.gimnasio.exception.BusinessException;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.MetodoPagoRepository;
import com.gimnasio.repository.SuscripcionRepository;
import com.gimnasio.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** CU-06 contratar, CU-07 renovación, CU-08 cancelar, CU-09 cambio de plan, CU-11 actualización. */
@Service
public class SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PlanService planService;
    private final PagoService pagoService;
    private final NotificacionService notificacionService;

    public SuscripcionService(SuscripcionRepository suscripcionRepository, UsuarioRepository usuarioRepository,
                              MetodoPagoRepository metodoPagoRepository, PlanService planService,
                              PagoService pagoService, NotificacionService notificacionService) {
        this.suscripcionRepository = suscripcionRepository;
        this.usuarioRepository = usuarioRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.planService = planService;
        this.pagoService = pagoService;
        this.notificacionService = notificacionService;
    }

    public Suscripcion obtenerActivaDe(Long usuarioId) {
        return suscripcionRepository.findFirstByUsuarioIdAndEstado(usuarioId, EstadoSuscripcion.ACTIVA)
                .orElseThrow(() -> new NotFoundException("El usuario no tiene una suscripción activa"));
    }

    public Suscripcion obtener(Long id) {
        return suscripcionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Suscripción no encontrada: " + id));
    }

    public Suscripcion obtenerDe(Long usuarioId, Long id) {
        Suscripcion suscripcion = obtener(id);
        verificarPropietario(usuarioId, suscripcion);
        return suscripcion;
    }

    public List<Suscripcion> listarDe(Long usuarioId) {
        return suscripcionRepository.findByUsuarioIdOrderByFechaInicioDesc(usuarioId);
    }

    /** CU-06: contratación con pago inmediato del primer periodo. */
    @Transactional
    public Suscripcion contratar(Long usuarioId, ContratarSuscripcionRequest solicitud) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + usuarioId));
        if (suscripcionRepository.findFirstByUsuarioIdAndEstado(usuarioId, EstadoSuscripcion.ACTIVA).isPresent()) {
            throw new BusinessException("Ya tiene una suscripción activa (RN-10)");
        }
        if (solicitud.renovacionAutomatica() && solicitud.idMetodo() == null) {
            throw new BusinessException("Para activar la renovación automática seleccione un método de pago (RN-01)");
        }
        MetodoPago metodo = solicitud.idMetodo() == null ? null : metodoDe(usuarioId, solicitud.idMetodo());
        Plan plan = planService.obtener(solicitud.idPlan());
        if (!plan.isActivo()) {
            throw new BusinessException("El plan seleccionado no está disponible");
        }

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setUsuario(usuario);
        suscripcion.setPlan(plan);
        LocalDate hoy = LocalDate.now();
        suscripcion.setFechaInicio(hoy);
        suscripcion.setFechaFin(hoy.plusDays(plan.getDuracionDias()));
        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
        suscripcion.setRenovacionAutomatica(solicitud.renovacionAutomatica());
        suscripcion.setIntentosCobro(0);
        suscripcionRepository.save(suscripcion);

        if (metodo != null) {
            ResultadoCobro resultado = pagoService.cobrar(suscripcion, metodo, TipoPago.ALTA, "ALTA-" + suscripcion.getId());
            if (!resultado.aprobado()) {
                throw new BusinessException("El cobro del plan fue rechazado: " + resultado.mensaje());
            }
        } else {
            // Pago en efectivo en recepción (CU-30); el alta queda activa y pendiente de caja.
            notificacionService.enviar(usuario, TipoNotificacion.RESULTADO_COBRO,
                    String.format("Suscripción %s activada. Realice el pago de $ %.2f en recepción (efectivo).",
                            plan.getNombre(), plan.getPrecio()));
        }
        notificacionService.enviar(usuario, TipoNotificacion.CANCELACION,
                String.format("Su plan %s está activo hasta el %s. Renovación automática: %s.",
                        plan.getNombre(), suscripcion.getFechaFin(),
                        solicitud.renovacionAutomatica() ? "activada" : "desactivada"));
        return suscripcion;
    }

    /** CU-30: venta presencial en recepción. */
    @Transactional
    public Suscripcion venderPresencial(Long recepcionistaId, VentaPresencialRequest solicitud) {
        Usuario recepcionista = usuarioRepository.findById(recepcionistaId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + recepcionistaId));
        if (recepcionista.getRol() != com.gimnasio.domain.enums.Rol.RECEPCIONISTA
                && recepcionista.getRol() != com.gimnasio.domain.enums.Rol.ADMINISTRADOR) {
            throw new BusinessException("Solo la recepción o administración pueden vender membresías presenciales");
        }

        ContratarSuscripcionRequest alta = new ContratarSuscripcionRequest(
                solicitud.idPlan(),
                solicitud.idMetodo(),
                solicitud.renovacionAutomatica()
        );

        Suscripcion suscripcion = contratar(solicitud.idUsuario(), alta);
        if (solicitud.observacion() != null && !solicitud.observacion().isBlank()) {
            notificacionService.enviar(suscripcion.getUsuario(), TipoNotificacion.CANCELACION,
                    "Venta presencial registrada por recepción: " + solicitud.observacion());
        }
        if (solicitud.pagoEfectivo()) {
            notificacionService.enviar(suscripcion.getUsuario(), TipoNotificacion.RESULTADO_COBRO,
                    String.format("Venta presencial registrada: pago en efectivo para %s. Debe confirmarse en caja.",
                            suscripcion.getPlan().getNombre()));
        }
        return suscripcion;
    }

    /** CU-08: cancelación desde la web/app cuando el usuario lo decida (opt-out siempre disponible). */
    @Transactional
    public Suscripcion cancelar(Long usuarioId, Long suscripcionId, String motivo) {
        Suscripcion suscripcion = obtener(suscripcionId);
        verificarPropietario(usuarioId, suscripcion);
        if (suscripcion.getEstado() != EstadoSuscripcion.ACTIVA) {
            throw new BusinessException("Solo se pueden cancelar suscripciones activas");
        }
        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        suscripcion.setFechaCancelacion(LocalDateTime.now());
        suscripcion.setMotivoCancelacion(motivo);
        suscripcion.setRenovacionAutomatica(false);
        suscripcionRepository.save(suscripcion);
        notificacionService.enviar(suscripcion.getUsuario(), TipoNotificacion.CANCELACION,
                String.format("Su suscripción fue cancelada. Mantiene el acceso hasta el %s y no se realizarán "
                        + "más cobros automáticos.", suscripcion.getFechaFin()));
        return suscripcion;
    }

    /** CU-09: cambio de plan con ajuste por diferencia de precio (pago tipo ajuste_cambio_plan). */
    @Transactional
    public Suscripcion cambiarPlan(Long usuarioId, Long suscripcionId, Long nuevoPlanId) {
        Suscripcion suscripcion = obtener(suscripcionId);
        verificarPropietario(usuarioId, suscripcion);
        if (suscripcion.getEstado() != EstadoSuscripcion.ACTIVA) {
            throw new BusinessException("Solo se puede cambiar el plan de una suscripción activa");
        }
        Plan nuevoPlan = planService.obtener(nuevoPlanId);
        if (!nuevoPlan.isActivo()) {
            throw new BusinessException("El plan seleccionado no está disponible");
        }
        Plan planActual = suscripcion.getPlan();
        if (planActual.getId().equals(nuevoPlan.getId())) {
            throw new BusinessException("La suscripción ya está en el plan " + nuevoPlan.getNombre());
        }

        BigDecimal diferencia = nuevoPlan.getPrecio().subtract(planActual.getPrecio());
        if (diferencia.compareTo(BigDecimal.ZERO) > 0) {
            MetodoPago metodo = metodoPredeterminado(usuarioId);
            ResultadoCobro resultado = pagoService.cobrar(suscripcion, metodo,
                    TipoPago.AJUSTE_CAMBIO_PLAN, "CAMBIO-" + suscripcion.getId());
            if (!resultado.aprobado()) {
                throw new BusinessException("El ajuste por cambio de plan fue rechazado: " + resultado.mensaje());
            }
        }

        suscripcion.setPlan(nuevoPlan);
        // Conserva la vigencia actual si es más larga; si el nuevo plan dura más, la extiende.
        LocalDate finNuevoPeriodo = LocalDate.now().plusDays(nuevoPlan.getDuracionDias());
        if (finNuevoPeriodo.isAfter(suscripcion.getFechaFin())) {
            suscripcion.setFechaFin(finNuevoPeriodo);
        }
        suscripcionRepository.save(suscripcion);
        notificacionService.enviar(suscripcion.getUsuario(), TipoNotificacion.CANCELACION,
                String.format("Cambio de plan realizado: %s. Nueva vigencia hasta %s.",
                        nuevoPlan.getNombre(), suscripcion.getFechaFin()));
        return suscripcion;
    }

    /** CU-11/RN-01: activar o desactivar la renovación automática (opt-in/opt-out explícito). */
    @Transactional
    public Suscripcion setRenovacionAutomatica(Long usuarioId, Long suscripcionId, boolean activa) {
        Suscripcion suscripcion = obtener(suscripcionId);
        verificarPropietario(usuarioId, suscripcion);
        if (suscripcion.getEstado() != EstadoSuscripcion.ACTIVA) {
            throw new BusinessException("La suscripción no está activa");
        }
        if (activa && metodoPredeterminadoOpcional(usuarioId) == null) {
            throw new BusinessException("Registre un método de pago antes de activar la renovación automática");
        }
        suscripcion.setRenovacionAutomatica(activa);
        if (activa) {
            suscripcion.setIntentosCobro(0);
        }
        suscripcionRepository.save(suscripcion);
        notificacionService.enviar(suscripcion.getUsuario(), TipoNotificacion.CANCELACION,
                "Renovación automática " + (activa ? "activada" : "desactivada") + " para su suscripción.");
        return suscripcion;
    }

    public List<Suscripcion> porVencer(int dias) {
        LocalDate hoy = LocalDate.now();
        return suscripcionRepository.findByEstadoAndFechaFinBetween(EstadoSuscripcion.ACTIVA, hoy, hoy.plusDays(dias));
    }

    public List<Suscripcion> vencidasSinRenovacion() {
        return suscripcionRepository.findByEstadoAndFechaFinBefore(EstadoSuscripcion.ACTIVA, LocalDate.now());
    }

    @Transactional
    public void marcarVencida(Suscripcion suscripcion) {
        suscripcion.setEstado(EstadoSuscripcion.VENCIDA);
        suscripcion.setRenovacionAutomatica(false);
        suscripcionRepository.save(suscripcion);
    }

    private MetodoPago metodoDe(Long usuarioId, Long metodoId) {
        MetodoPago metodo = metodoPagoRepository.findById(metodoId)
                .orElseThrow(() -> new NotFoundException("Método de pago no encontrado: " + metodoId));
        if (!metodo.getUsuario().getId().equals(usuarioId) || !metodo.isActivo()) {
            throw new BusinessException("El método de pago no pertenece al usuario o está inactivo");
        }
        return metodo;
    }

    private MetodoPago metodoPredeterminado(Long usuarioId) {
        MetodoPago metodo = metodoPredeterminadoOpcional(usuarioId);
        if (metodo == null) {
            throw new BusinessException("Registre un método de pago para continuar");
        }
        return metodo;
    }

    private MetodoPago metodoPredeterminadoOpcional(Long usuarioId) {
        List<MetodoPago> metodos = metodoPagoRepository.findByUsuarioIdAndActivoTrue(usuarioId);
        return metodos.stream().filter(MetodoPago::isPredeterminado).findFirst()
                .orElse(metodos.isEmpty() ? null : metodos.get(0));
    }

    private void verificarPropietario(Long usuarioId, Suscripcion suscripcion) {
        if (!suscripcion.getUsuario().getId().equals(usuarioId)) {
            throw new NotFoundException("Suscripción no encontrada");
        }
    }
}
