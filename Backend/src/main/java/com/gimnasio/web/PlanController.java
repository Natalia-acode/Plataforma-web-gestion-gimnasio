package com.gimnasio.web;

import com.gimnasio.domain.Plan;
import com.gimnasio.dto.PlanRequest;
import com.gimnasio.dto.PlanResponse;
import com.gimnasio.service.PlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** CU-05 catálogo de planes; CU-27 gestión (administrador). */
@RestController
@RequestMapping("/api/planes")
@Tag(name = "Planes")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public List<PlanResponse> listar() {
        return planService.listarActivos().stream().map(this::mapear).toList();
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<PlanResponse> listarTodos() {
        return planService.listarTodos().stream().map(this::mapear).toList();
    }

    @GetMapping("/{id}")
    public PlanResponse obtener(@PathVariable Long id) {
        return mapear(planService.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanResponse crear(@Valid @RequestBody PlanRequest solicitud) {
        return mapear(planService.crear(solicitud));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public PlanResponse actualizar(@PathVariable Long id, @Valid @RequestBody PlanRequest solicitud) {
        return mapear(planService.actualizar(id, solicitud));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public PlanResponse desactivar(@PathVariable Long id) {
        return mapear(planService.desactivar(id));
    }

    private PlanResponse mapear(Plan plan) {
        return new PlanResponse(plan.getId(), plan.getNombre(), plan.getDescripcion(),
                plan.getPrecio(), plan.getDuracionDias(), plan.isActivo());
    }
}
