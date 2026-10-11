package com.gimnasio.service;

import com.gimnasio.domain.Notificacion;
import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.enums.TipoNotificacion;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.NotificacionRepository;
import com.gimnasio.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Persiste la notificación para el centro de avisos y delega el envío
 * externo al Servicio de Notificaciones (stub: aquí iría el proveedor real).
 */
@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacionService(NotificacionRepository notificacionRepository,
                               UsuarioRepository usuarioRepository) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public void enviar(Usuario usuario, TipoNotificacion tipo, String mensaje) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setTipo(tipo);
        notificacion.setMensaje(mensaje);
        notificacionRepository.save(notificacion);
        log.info("Notificación [{}] para {} <{}>: {}",
                tipo.getValor(), usuario.getNombreCompleto(), usuario.getEmail(), mensaje);
    }

    public void enviarA(Long usuarioId, TipoNotificacion tipo, String mensaje) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + usuarioId));
        enviar(usuario, tipo, mensaje);
    }

    public List<Notificacion> listar(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaEnvioDesc(usuarioId);
    }

    public List<Notificacion> listarNoLeidas(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaEnvioDesc(usuarioId);
    }

    public void marcarLeida(Long usuarioId, Long notificacionId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new NotFoundException("Notificación no encontrada: " + notificacionId));
        if (!notificacion.getUsuario().getId().equals(usuarioId)) {
            throw new NotFoundException("Notificación no encontrada: " + notificacionId);
        }
        notificacion.setLeida(true);
        notificacionRepository.save(notificacion);
    }
}
