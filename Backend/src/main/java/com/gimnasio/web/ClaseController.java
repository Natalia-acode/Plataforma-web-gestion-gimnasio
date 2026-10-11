package com.gimnasio.web;

import com.gimnasio.domain.ReservaClase;
import com.gimnasio.dto.ClaseRequest;
import com.gimnasio.dto.ClaseResponse;
import com.gimnasio.dto.ReservaRequest;
import com.gimnasio.dto.ReservaResponse;
import com.gimnasio.security.AuthenticatedUser;
import com.gimnasio.service.ClaseService;
import com.gimnasio.service.ReservaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** CU-18 reservar, CU-19 cancelar, CU-20 lista de espera, CU-21 gestión de clases. */
@RestController
@RequestMapping("/api/clases")
@Tag(name = "Clases y reservas")
public class ClaseController {

    private final ClaseService claseService;
    private final ReservaService reservaService;

    public ClaseController(ClaseService claseService, ReservaService reservaService) {
        this.claseService = claseService;
        this.reservaService = reservaService;
    }

    /** Horario de clases (público); el cupo mostrado es para hoy. */
    @GetMapping
    public List<ClaseResponse> listar(@RequestParam(required = false) String dia) {
        LocalDate hoy = LocalDate.now();
        return claseService.listar(dia).stream()
                .map(clase -> ClaseResponse.desde(clase, claseService.reservasConfirmadas(clase, hoy)))
                .toList();
    }

    @GetMapping("/{id}")
    public ClaseResponse obtener(@PathVariable Long id) {
        return ClaseResponse.desde(claseService.obtener(id),
                claseService.reservasConfirmadas(claseService.obtener(id), LocalDate.now()));
    }

    /** CU-20: listado de asistentes para pasar lista (entrenador). */
    @GetMapping("/{id}/asistentes")
    public List<ReservaResponse> asistentes(@PathVariable Long id, @RequestParam String fecha) {
        return reservaService.asistentes(id, LocalDate.parse(fecha)).stream()
                .map(ReservaResponse::desde).toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ENTRENADOR', 'ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public ClaseResponse crear(@Valid @RequestBody ClaseRequest solicitud) {
        return ClaseResponse.desde(claseService.crear(solicitud), 0);
    }

    /** CU-18: reservar (entra a lista de espera si no hay cupo, RN-09). */
    @PostMapping("/{id}/reservas")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse reservar(@AuthenticationPrincipal AuthenticatedUser principal,
                                    @PathVariable Long id,
                                    @Valid @RequestBody ReservaRequest solicitud) {
        ReservaClase reserva = reservaService.reservar(principal.getId(), id, solicitud.fecha());
        return ReservaResponse.desde(reserva);
    }

    @GetMapping("/reservas/mias")
    public List<ReservaResponse> misReservas(@AuthenticationPrincipal AuthenticatedUser principal) {
        return reservaService.listarDe(principal.getId()).stream()
                .map(ReservaResponse::desde).toList();
    }

    @DeleteMapping("/reservas/{reservaId}")
    public ReservaResponse cancelarReserva(@AuthenticationPrincipal AuthenticatedUser principal,
                                           @PathVariable Long reservaId) {
        return ReservaResponse.desde(reservaService.cancelar(principal.getId(), reservaId));
    }

    /** CU-20: marcar asistencia/no asistencia (entrenador). */
    @PostMapping("/reservas/{reservaId}/asistencia")
    @PreAuthorize("hasAnyRole('ENTRENADOR', 'ADMINISTRADOR')")
    public ReservaResponse marcarAsistencia(@PathVariable Long reservaId,
                                            @RequestParam boolean asistio) {
        return ReservaResponse.desde(reservaService.marcarAsistencia(reservaId, asistio));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENTRENADOR', 'ADMINISTRADOR')")
    public ClaseResponse desactivar(@PathVariable Long id) {
        return ClaseResponse.desde(claseService.desactivar(id), 0);
    }
}
