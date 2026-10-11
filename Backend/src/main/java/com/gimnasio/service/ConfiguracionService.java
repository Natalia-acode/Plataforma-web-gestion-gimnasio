package com.gimnasio.service;

import com.gimnasio.domain.Configuracion;
import com.gimnasio.dto.AforoConfigRequest;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.ConfiguracionRepository;
import org.springframework.stereotype.Service;

/** CU-17: parámetros del aforo y de las operaciones. */
@Service
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

    public ConfiguracionService(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    public Configuracion obtener() {
        return configuracionRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NotFoundException("No hay configuración del sistema"));
    }

    public Configuracion actualizar(AforoConfigRequest solicitud) {
        Configuracion configuracion = obtener();
        if (solicitud.capacidadMaxima() != null) {
            configuracion.setCapacidadMaxima(solicitud.capacidadMaxima());
        }
        if (solicitud.umbralMedio() != null) {
            configuracion.setUmbralMedio(solicitud.umbralMedio().byteValue());
        }
        if (solicitud.umbralAlto() != null) {
            configuracion.setUmbralAlto(solicitud.umbralAlto().byteValue());
        }
        if (solicitud.ventanaTurnoMin() != null) {
            configuracion.setVentanaTurnoMin(solicitud.ventanaTurnoMin());
        }
        if (solicitud.tiempoRotacionMin() != null) {
            configuracion.setTiempoRotacionMin(solicitud.tiempoRotacionMin());
        }
        if (solicitud.reintentosCobro() != null) {
            configuracion.setReintentosCobro(solicitud.reintentosCobro());
        }
        if (configuracion.getUmbralAlto() < configuracion.getUmbralMedio()) {
            throw new com.gimnasio.exception.BusinessException(
                    "El umbral alto debe ser mayor o igual al umbral medio");
        }
        return configuracionRepository.save(configuracion);
    }
}
