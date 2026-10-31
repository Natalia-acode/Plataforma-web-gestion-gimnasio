package com.gimnasio.service;

import com.gimnasio.domain.Zona;
import com.gimnasio.domain.enums.TipoZona;
import com.gimnasio.dto.ZonaRequest;
import com.gimnasio.exception.BusinessException;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.ZonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ZonaService {

    private final ZonaRepository zonaRepository;

    public ZonaService(ZonaRepository zonaRepository) {
        this.zonaRepository = zonaRepository;
    }

    public List<Zona> listarActivas() {
        return zonaRepository.findByActivaTrue();
    }

    public List<Zona> listarTodos() {
        return zonaRepository.findAll();
    }

    public Zona obtener(Long id) {
        return zonaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Zona no encontrada: " + id));
    }

    @Transactional
    public Zona crear(ZonaRequest solicitud) {
        TipoZona tipo = TipoZona.desdeValor(solicitud.tipo());
        Zona zona = new Zona();
        zona.setNombre(solicitud.nombre().trim());
        zona.setTipo(tipo);
        zona.setCapacidadMaxima(solicitud.capacidadMaxima());
        zona.setActiva(solicitud.activa() == null || solicitud.activa());
        return zonaRepository.save(zona);
    }

    @Transactional
    public Zona actualizar(Long id, ZonaRequest solicitud) {
        Zona zona = obtener(id);
        zona.setNombre(solicitud.nombre().trim());
        zona.setTipo(TipoZona.desdeValor(solicitud.tipo()));
        zona.setCapacidadMaxima(solicitud.capacidadMaxima());
        zona.setActiva(solicitud.activa() == null || solicitud.activa());
        return zonaRepository.save(zona);
    }

    @Transactional
    public Zona desactivar(Long id) {
        Zona zona = obtener(id);
        if (!zona.isActiva()) {
            throw new BusinessException("La zona ya está desactivada");
        }
        zona.setActiva(false);
        return zonaRepository.save(zona);
    }
}
