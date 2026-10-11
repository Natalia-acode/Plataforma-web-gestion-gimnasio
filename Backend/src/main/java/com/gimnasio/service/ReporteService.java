package com.gimnasio.service;

import com.gimnasio.domain.Pago;
import com.gimnasio.domain.Suscripcion;
import com.gimnasio.domain.enums.EstadoPago;
import com.gimnasio.domain.enums.EstadoReserva;
import com.gimnasio.domain.enums.EstadoSuscripcion;
import com.gimnasio.dto.PagoResponse;
import com.gimnasio.dto.ReporteResponse;
import com.gimnasio.repository.CheckInRepository;
import com.gimnasio.repository.PagoRepository;
import com.gimnasio.repository.ReservaClaseRepository;
import com.gimnasio.repository.SuscripcionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** CU-29: reportes de ingresos, ocupación y uso (administrador). */
@Service
public class ReporteService {

    private final PagoRepository pagoRepository;
    private final CheckInRepository checkInRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final ReservaClaseRepository reservaClaseRepository;

    public ReporteService(PagoRepository pagoRepository, CheckInRepository checkInRepository,
                          SuscripcionRepository suscripcionRepository,
                          ReservaClaseRepository reservaClaseRepository) {
        this.pagoRepository = pagoRepository;
        this.checkInRepository = checkInRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.reservaClaseRepository = reservaClaseRepository;
    }

    public ReporteResponse generar(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();

        List<Pago> pagos = pagoRepository.findByFechaPagoBetweenOrderByFechaPagoDesc(inicio, fin);
        List<Pago> aprobados = pagos.stream().filter(p -> p.getEstado() == EstadoPago.APROBADO).toList();
        BigDecimal ingresos = aprobados.stream()
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long checkIns = checkInRepository.countByFechaHoraEntradaBetween(inicio, fin);

        List<Suscripcion> nuevas = suscripcionRepository
                .findByEstadoAndFechaFinBetween(EstadoSuscripcion.ACTIVA, desde, hasta).stream()
                .filter(s -> !s.getFechaInicio().isBefore(desde))
                .toList();
        long canceladas = suscripcionRepository.findAll().stream()
                .filter(s -> s.getFechaCancelacion() != null)
                .filter(s -> !s.getFechaCancelacion().isBefore(inicio) && s.getFechaCancelacion().isBefore(fin))
                .count();

        List<com.gimnasio.domain.ReservaClase> reservasDelPeriodo = reservaClaseRepository.findAll().stream()
                .filter(r -> !r.getFecha().isBefore(desde) && !r.getFecha().isAfter(hasta))
                .toList();
        long reservas = reservasDelPeriodo.size();
        long inasistencias = reservasDelPeriodo.stream()
                .filter(r -> r.getEstado() == EstadoReserva.NO_ASISTIO)
                .count();

        List<PagoResponse> pagosDto = pagos.stream()
                .map(p -> PagoResponse.desde(p, null))
                .toList();

        return new ReporteResponse(
                desde.toString(),
                hasta.toString(),
                aprobados.size(),
                ingresos,
                checkIns,
                nuevas.size(),
                canceladas,
                reservas,
                inasistencias,
                pagosDto);
    }

    /** CU-29 (exportación): CSV de los pagos del periodo. */
    public String exportarPagosCsv(LocalDate desde, LocalDate hasta) {
        List<Pago> pagos = pagoRepository.findByFechaPagoBetweenOrderByFechaPagoDesc(
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
        StringBuilder csv = new StringBuilder();
        csv.append("Factura,Fecha,Plan,Monto,Tipo,Estado,ID transaccion\n");
        for (Pago pago : pagos) {
            csv.append(pago.getNumeroFactura() == null ? "" : pago.getNumeroFactura()).append(',')
                    .append(pago.getFechaPago()).append(',')
                    .append(pago.getSuscripcion().getPlan().getNombre()).append(',')
                    .append(pago.getMonto()).append(',')
                    .append(pago.getTipo().getValor()).append(',')
                    .append(pago.getEstado().getValor()).append(',')
                    .append(pago.getIdTransaccionPasarela() == null ? "" : pago.getIdTransaccionPasarela())
                    .append('\n');
        }
        return csv.toString();
    }
}
