package com.gimnasio.service;

import com.gimnasio.domain.MetodoPago;
import com.gimnasio.domain.Usuario;
import com.gimnasio.dto.MetodoPagoRequest;
import com.gimnasio.exception.BusinessException;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.MetodoPagoRepository;
import com.gimnasio.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** CU-10: registro y gestión de tarjetas tokenizadas (RNF-02). */
@Service
public class MetodoPagoService {

    private final MetodoPagoRepository metodoPagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasarelaPagoClient pasarela;

    public MetodoPagoService(MetodoPagoRepository metodoPagoRepository, UsuarioRepository usuarioRepository,
                             PasarelaPagoClient pasarela) {
        this.metodoPagoRepository = metodoPagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.pasarela = pasarela;
    }

    public List<MetodoPago> listarDe(Long usuarioId) {
        return metodoPagoRepository.findByUsuarioIdAndActivoTrue(usuarioId);
    }

    @Transactional
    public MetodoPago registrar(Long usuarioId, MetodoPagoRequest solicitud) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + usuarioId));
        PasarelaPagoClient.Token token = pasarela.tokenizarTarjeta(solicitud.numeroTarjeta());

        boolean primero = metodoPagoRepository.findByUsuarioIdAndActivoTrue(usuarioId).isEmpty();
        if (solicitud.predeterminado() || primero) {
            desmarcarPredeterminados(usuarioId);
        }

        MetodoPago metodo = new MetodoPago();
        metodo.setUsuario(usuario);
        metodo.setTipo(com.gimnasio.domain.enums.TipoMetodoPago.desdeValor(solicitud.tipo().toLowerCase()));
        metodo.setUltimosCuatro(token.ultimosCuatro());
        metodo.setTitular(solicitud.titular());
        metodo.setTokenPasarela(token.token());
        metodo.setPredeterminado(solicitud.predeterminado() || primero);
        return metodoPagoRepository.save(metodo);
    }

    @Transactional
    public MetodoPago marcarPredeterminado(Long usuarioId, Long metodoId) {
        MetodoPago metodo = obtenerDe(usuarioId, metodoId);
        desmarcarPredeterminados(usuarioId);
        metodo.setPredeterminado(true);
        return metodoPagoRepository.save(metodo);
    }

    @Transactional
    public void eliminar(Long usuarioId, Long metodoId) {
        MetodoPago metodo = obtenerDe(usuarioId, metodoId);
        long activos = metodoPagoRepository.findByUsuarioIdAndActivoTrue(usuarioId).size();
        if (activos <= 1) {
            throw new BusinessException("Debe conservar al menos un método de pago activo");
        }
        metodo.setActivo(false);
        metodo.setPredeterminado(false);
        metodoPagoRepository.save(metodo);
    }

    private MetodoPago obtenerDe(Long usuarioId, Long metodoId) {
        MetodoPago metodo = metodoPagoRepository.findById(metodoId)
                .orElseThrow(() -> new NotFoundException("Método de pago no encontrado: " + metodoId));
        if (!metodo.getUsuario().getId().equals(usuarioId)) {
            throw new NotFoundException("Método de pago no encontrado: " + metodoId);
        }
        return metodo;
    }

    private void desmarcarPredeterminados(Long usuarioId) {
        for (MetodoPago existente : metodoPagoRepository.findByUsuarioIdAndActivoTrue(usuarioId)) {
            if (existente.isPredeterminado()) {
                existente.setPredeterminado(false);
                metodoPagoRepository.save(existente);
            }
        }
    }
}
