package com.gimnasio.service;

import com.gimnasio.domain.MetodoPago;
import com.gimnasio.domain.Pago;
import com.gimnasio.domain.Suscripcion;
import com.gimnasio.domain.enums.EstadoPago;
import com.gimnasio.domain.enums.TipoNotificacion;
import com.gimnasio.domain.enums.TipoPago;
import com.gimnasio.dto.PagoResponse;
import com.gimnasio.dto.ResultadoCobro;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** CU-06, CU-11, CU-12, CU-30: registro de cobros, facturación y reintentos. */
@Service
public class PagoService {

    private static final AtomicLong SECUENCIA_FACTURA = new AtomicLong(1);
    private static final DateTimeFormatter FORMATO_FACTURA = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PagoRepository pagoRepository;
    private final PasarelaPagoClient pasarela;
    private final NotificacionService notificacionService;

    public PagoService(PagoRepository pagoRepository, PasarelaPagoClient pasarela,
                       NotificacionService notificacionService) {
        this.pagoRepository = pagoRepository;
        this.pasarela = pasarela;
        this.notificacionService = notificacionService;
    }

    /**
     * Cobra en la pasarela y registra el pago. Nunca lanza por rechazo:
     * devuelve ResultadoCobro para que el orquestador (alta, renovación)
     * decida el efecto sobre la suscripción (RN-03, RN-04).
     */
    @Transactional
    public ResultadoCobro cobrar(Suscripcion suscripcion, MetodoPago metodo, TipoPago tipo, String referencia) {
        Pago pago = new Pago();
        pago.setSuscripcion(suscripcion);
        pago.setMetodo(metodo);
        pago.setMonto(suscripcion.getPlan().getPrecio());
        pago.setTipo(tipo);
        pago.setFechaPago(LocalDateTime.now());

        PasarelaPagoClient.Transaccion transaccion =
                pasarela.cobrar(metodo.getTokenPasarela(), pago.getMonto(), referencia);
        pago.setIdTransaccionPasarela(transaccion.idTransaccion());

        if (transaccion.aprobada()) {
            pago.setEstado(EstadoPago.APROBADO);
            pago.setNumeroFactura(generarNumeroFactura());
            pagoRepository.save(pago);
            notificacionService.enviar(suscripcion.getUsuario(), TipoNotificacion.RESULTADO_COBRO,
                    String.format("Cobro %s aprobado por $ %.2f (%s). Factura %s.",
                            tipo.getValor(), pago.getMonto(), suscripcion.getPlan().getNombre(),
                            pago.getNumeroFactura()));
            return ResultadoCobro.aprobado(PagoResponse.desde(pago, null), pago.getMonto());
        }

        pago.setEstado(EstadoPago.RECHAZADO);
        pagoRepository.save(pago);
        return ResultadoCobro.rechazado(pago.getMonto(), transaccion.mensaje());
    }

    public List<Pago> listarDeUsuario(Long usuarioId) {
        return pagoRepository.findBySuscripcionUsuarioIdOrderByFechaPagoDesc(usuarioId);
    }

    public Pago obtener(Long pagoId) {
        return pagoRepository.findById(pagoId)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado: " + pagoId));
    }

    public Pago obtenerDeUsuario(Long usuarioId, Long pagoId) {
        Pago pago = obtener(pagoId);
        if (!pago.getSuscripcion().getUsuario().getId().equals(usuarioId)) {
            throw new NotFoundException("Pago no encontrado: " + pagoId);
        }
        return pago;
    }

    public List<Pago> listarEntre(LocalDateTime desde, LocalDateTime hasta) {
        return pagoRepository.findByFechaPagoBetweenOrderByFechaPagoDesc(desde, hasta);
    }

    public List<Pago> listarAprobadosEntre(LocalDateTime desde, LocalDateTime hasta) {
        return pagoRepository.findByEstadoAndFechaPagoBetween(EstadoPago.APROBADO, desde, hasta);
    }

    private String generarNumeroFactura() {
        return "FAC-" + LocalDateTime.now().format(FORMATO_FACTURA) + "-"
                + String.format("%06d", SECUENCIA_FACTURA.getAndIncrement());
    }
}
