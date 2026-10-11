package com.gimnasio.service;

import com.gimnasio.domain.MetodoPago;
import com.gimnasio.domain.Suscripcion;
import com.gimnasio.domain.enums.TipoNotificacion;
import com.gimnasio.domain.enums.TipoPago;
import com.gimnasio.dto.ResultadoCobro;
import com.gimnasio.repository.MetodoPagoRepository;
import com.gimnasio.repository.SuscripcionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * RN-04: renovación automática con reintentos a +1, +3 y +7 días.
 * Tras agotar los reintentos la suscripción vence y el usuario recibe notificación.
 * RN-01: solo se cobra a suscripciones con renovación automática activada (opt-in).
 */
@Service
public class CobroService {

    private static final Logger log = LoggerFactory.getLogger(CobroService.class);
    private static final int[] DIAS_REINTENTO = {1, 3, 7};

    private final SuscripcionRepository suscripcionRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PagoService pagoService;
    private final NotificacionService notificacionService;
    private final ConfiguracionService configuracionService;

    public CobroService(SuscripcionRepository suscripcionRepository, MetodoPagoRepository metodoPagoRepository,
                        PagoService pagoService, NotificacionService notificacionService,
                        ConfiguracionService configuracionService) {
        this.suscripcionRepository = suscripcionRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.pagoService = pagoService;
        this.notificacionService = notificacionService;
        this.configuracionService = configuracionService;
    }

    /** Recordatorio previo al vencimiento (CU-12, flujo normal). */
    @Transactional
    public int enviarRecordatorios(int diasAntes) {
        List<Suscripcion> porVencer = suscripcionRepository.findByEstadoAndFechaFinBetween(
                com.gimnasio.domain.enums.EstadoSuscripcion.ACTIVA,
                LocalDate.now(), LocalDate.now().plusDays(diasAntes));
        int enviados = 0;
        for (Suscripcion suscripcion : porVencer) {
            if (suscripcion.isRenovacionAutomatica()) {
                notificacionService.enviar(suscripcion.getUsuario(), TipoNotificacion.RECORDATORIO_COBRO,
                        String.format("Su plan %s vence el %s. Se renovará automáticamente por $ %.2f.",
                                suscripcion.getPlan().getNombre(), suscripcion.getFechaFin(),
                                suscripcion.getPlan().getPrecio()));
                enviados++;
            }
        }
        return enviados;
    }

    /**
     * Ejecución diaria: cobra las suscripciones cuya fecha_fin llegó,
     * aplicando los reintentos configurados. Devuelve el resumen.
     */
    @Transactional
    public List<ResultadoCobro> ejecutarCobros() {
        int maxReintentos = configuracionService.obtener().getReintentosCobro();
        List<Suscripcion> candidatas = suscripcionRepository.findByEstadoAndFechaFinBefore(
                com.gimnasio.domain.enums.EstadoSuscripcion.ACTIVA, LocalDate.now().plusDays(1));

        List<ResultadoCobro> resultados = new java.util.ArrayList<>();
        for (Suscripcion suscripcion : candidatas) {
            if (suscripcion.getFechaFin().isAfter(LocalDate.now())) {
                continue;   // aún vigente
            }
            if (!suscripcion.isRenovacionAutomatica()) {
                // Sin opt-in: al vencer pasa a vencida y se invita a renovar manualmente.
                notificacionService.enviar(suscripcion.getUsuario(), TipoNotificacion.RECORDATORIO_COBRO,
                        String.format("Su plan %s venció. Renueve desde la aplicación para seguir entrenando.",
                                suscripcion.getPlan().getNombre()));
                marcarVencida(suscripcion);
                continue;
            }

            int intento = suscripcion.getIntentosCobro();
            if (intento >= maxReintentos) {
                notificacionService.enviar(suscripcion.getUsuario(), TipoNotificacion.RESULTADO_COBRO,
                        "No fue posible renovar su plan tras varios intentos. La suscripción fue suspendida; "
                                + "actualice su método de pago y renueve manualmente.");
                marcarVencida(suscripcion);
                continue;
            }

            MetodoPago metodo = metodoPredeterminado(suscripcion.getUsuario().getId());
            if (metodo == null) {
                marcarVencida(suscripcion);
                continue;
            }

            // Se reintenta solo cuando corresponde según la escala +1/+3/+7 días.
            int diasEsperados = DIAS_REINTENTO[Math.min(intento, DIAS_REINTENTO.length - 1)];
            long diasDesdeVencimiento = java.time.temporal.ChronoUnit.DAYS.between(suscripcion.getFechaFin(), LocalDate.now());
            if (diasDesdeVencimiento < diasEsperados) {
                continue;
            }

            ResultadoCobro resultado = pagoService.cobrar(suscripcion, metodo, TipoPago.RENOVACION,
                    "RENOVACION-" + suscripcion.getId() + "-" + intento);
            resultados.add(resultado);
            if (resultado.aprobado()) {
                suscripcion.setFechaInicio(LocalDate.now());
                suscripcion.setFechaFin(LocalDate.now().plusDays(suscripcion.getPlan().getDuracionDias()));
                suscripcion.setIntentosCobro(0);
                suscripcionRepository.save(suscripcion);
                log.info("Renovación automática aprobada para suscripción {}", suscripcion.getId());
            } else {
                suscripcion.setIntentosCobro(intento + 1);
                suscripcionRepository.save(suscripcion);
                notificacionService.enviar(suscripcion.getUsuario(), TipoNotificacion.RESULTADO_COBRO,
                        String.format("Cobro de renovación rechazado (%s). Intento %d de %d; "
                                        + "actualice su método de pago para evitar la suspensión.",
                                resultado.mensaje(), intento + 1, maxReintentos));
            }
        }
        return resultados;
    }

    private void marcarVencida(Suscripcion suscripcion) {
        suscripcion.setEstado(com.gimnasio.domain.enums.EstadoSuscripcion.VENCIDA);
        suscripcion.setRenovacionAutomatica(false);
        suscripcionRepository.save(suscripcion);
    }

    private MetodoPago metodoPredeterminado(Long usuarioId) {
        List<MetodoPago> metodos = metodoPagoRepository.findByUsuarioIdAndActivoTrue(usuarioId);
        return metodos.stream().filter(MetodoPago::isPredeterminado).findFirst()
                .orElse(metodos.isEmpty() ? null : metodos.get(0));
    }
}
