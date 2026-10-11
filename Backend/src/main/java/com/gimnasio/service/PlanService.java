package com.gimnasio.service;

import com.gimnasio.domain.Plan;
import com.gimnasio.dto.PlanRequest;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** CU-05 catálogo de planes; CU-27 gestión de planes (administrador). */
@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<Plan> listarActivos() {
        return planRepository.findByActivoTrue();
    }

    public List<Plan> listarTodos() {
        return planRepository.findAll();
    }

    public Plan obtener(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plan no encontrado: " + id));
    }

    public Plan crear(PlanRequest solicitud) {
        Plan plan = new Plan();
        aplicar(plan, solicitud);
        return planRepository.save(plan);
    }

    public Plan actualizar(Long id, PlanRequest solicitud) {
        Plan plan = obtener(id);
        aplicar(plan, solicitud);
        return planRepository.save(plan);
    }

    public Plan desactivar(Long id) {
        Plan plan = obtener(id);
        plan.setActivo(false);
        return planRepository.save(plan);
    }

    private void aplicar(Plan plan, PlanRequest solicitud) {
        plan.setNombre(solicitud.nombre());
        plan.setDescripcion(solicitud.descripcion());
        plan.setPrecio(solicitud.precio());
        plan.setDuracionDias(solicitud.duracionDias());
        if (solicitud.activo() != null) {
            plan.setActivo(solicitud.activo());
        }
    }
}
