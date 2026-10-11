package com.gimnasio.web;

import com.gimnasio.domain.CheckIn;
import com.gimnasio.dto.CheckInManualRequest;
import com.gimnasio.dto.CheckInResponse;
import com.gimnasio.security.AuthenticatedUser;
import com.gimnasio.service.CheckInService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** CU-13 entrada (QR y manual), CU-14 salida. Recepción y administración. */
@RestController
@RequestMapping("/api/checkin")
@Tag(name = "Check-in")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    /** Entrada con QR escaneado en recepción (a nombre del usuario autenticado). */
    @PostMapping("/qr")
    @ResponseStatus(HttpStatus.CREATED)
    public CheckInResponse checkInQr(@AuthenticationPrincipal AuthenticatedUser principal) {
        return mapear(checkInService.registrarEntradaQr(principal.getId()));
    }

    /** Entrada manual: recepción registra el ingreso de un cliente. */
    @PostMapping("/manual")
    @ResponseStatus(HttpStatus.CREATED)
    public CheckInResponse checkInManual(@AuthenticationPrincipal AuthenticatedUser principal,
                                         @Valid @RequestBody CheckInManualRequest solicitud) {
        return mapear(checkInService.registrarEntradaManual(solicitud.idUsuario(), principal.getId()));
    }

    /** Salida del usuario indicado. */
    @PostMapping("/{usuarioId}/salida")
    public CheckInResponse checkOut(@PathVariable Long usuarioId) {
        return mapear(checkInService.registrarSalida(usuarioId));
    }

    /** Personas actualmente dentro (vista de recepción). */
    @GetMapping("/dentro")
    public List<CheckInResponse> dentro() {
        return checkInService.dentroDelGimnasio().stream().map(this::mapear).toList();
    }

    @GetMapping("/hoy")
    public List<CheckInResponse> hoy() {
        return checkInService.ingresosDelDia().stream().map(this::mapear).toList();
    }

    private CheckInResponse mapear(CheckIn checkIn) {
        return new CheckInResponse(
                checkIn.getId(),
                checkIn.getUsuario().getId(),
                checkIn.getUsuario().getNombreCompleto(),
                checkIn.getFechaHoraEntrada().toString(),
                checkIn.getFechaHoraSalida() == null ? null : checkIn.getFechaHoraSalida().toString(),
                checkIn.getMetodo().getValor());
    }
}
