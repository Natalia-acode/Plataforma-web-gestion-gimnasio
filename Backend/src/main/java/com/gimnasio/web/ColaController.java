package com.gimnasio.web;

import com.gimnasio.domain.TurnoCola;
import com.gimnasio.dto.TurnoRequest;
import com.gimnasio.dto.TurnoResponse;
import com.gimnasio.security.AuthenticatedUser;
import com.gimnasio.service.ColaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** CU-22 a CU-25: cola virtual (problema 3). */
@RestController
@RequestMapping("/api/cola")
@Tag(name = "Cola virtual")
public class ColaController {

    private final ColaService colaService;

    public ColaController(ColaService colaService) {
        this.colaService = colaService;
    }

    /** CU-22: solicitar turno virtual (ingreso o clase). */
    @PostMapping("/turno")
    @ResponseStatus(HttpStatus.CREATED)
    public TurnoResponse solicitar(@AuthenticationPrincipal AuthenticatedUser principal,
                                   @Valid @RequestBody TurnoRequest solicitud) {
        return mapear(colaService.solicitar(principal.getId(), solicitud.tipo()));
    }

    /** CU-23: mi turno actual con posición y espera estimada. */
    @GetMapping("/turno/mi")
    public ResponseEntity<TurnoResponse> miTurno(@AuthenticationPrincipal AuthenticatedUser principal) {
        TurnoCola turno = colaService.miTurno(principal.getId());
        return turno == null ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(mapear(turno));
    }

    /** CU-24: cancelar mi turno. */
    @DeleteMapping("/turno")
    public TurnoResponse cancelar(@AuthenticationPrincipal AuthenticatedUser principal) {
        return mapear(colaService.cancelar(principal.getId()));
    }

    // ---------- Operación desde recepción (CU-25) ----------

    @GetMapping("/gestion/cola")
    public List<TurnoResponse> cola() {
        return colaService.colaCompleta().stream().map(this::mapear).toList();
    }

    @PostMapping("/gestion/llamar-siguiente")
    public TurnoResponse llamarSiguiente() {
        return mapear(colaService.llamarSiguiente());
    }

    @PostMapping("/gestion/{turnoId}/atendido")
    public TurnoResponse atendido(@PathVariable Long turnoId) {
        return mapear(colaService.marcarAtendido(turnoId));
    }

    private TurnoResponse mapear(TurnoCola turno) {
        return TurnoResponse.desde(turno, colaService.personasDelante(turno),
                colaService.esperaEstimadaMin(turno));
    }
}
