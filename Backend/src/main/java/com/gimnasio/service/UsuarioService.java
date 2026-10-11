package com.gimnasio.service;

import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.enums.EstadoUsuario;
import com.gimnasio.domain.enums.Rol;
import com.gimnasio.domain.enums.TipoNotificacion;
import com.gimnasio.exception.BusinessException;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** CU-26 administración de usuarios del personal (rol, suspensión). */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;

    public UsuarioService(UsuarioRepository usuarioRepository, NotificacionService notificacionService) {
        this.usuarioRepository = usuarioRepository;
        this.notificacionService = notificacionService;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarPorRol(Rol rol) {
        return usuarioRepository.findByRol(rol);
    }

    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + id));
    }

    @Transactional
    public Usuario suspender(Long id, Long administradorId) {
        if (id.equals(administradorId)) {
            throw new BusinessException("No puede suspender su propia cuenta");
        }
        Usuario usuario = obtener(id);
        usuario.setEstado(EstadoUsuario.SUSPENDIDO);
        usuarioRepository.save(usuario);
        notificacionService.enviar(usuario, TipoNotificacion.CUENTA,
                "Su cuenta fue suspendida. Contacte a la administración del gimnasio.");
        return usuario;
    }

    @Transactional
    public Usuario reactivar(Long id) {
        Usuario usuario = obtener(id);
        usuario.setEstado(EstadoUsuario.ACTIVO);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario asignarRol(Long id, String rol) {
        Usuario usuario = obtener(id);
        usuario.setRol(Rol.desdeValor(rol.toLowerCase()));
        usuarioRepository.save(usuario);
        notificacionService.enviar(usuario, TipoNotificacion.CUENTA,
                "Su rol en el sistema fue actualizado a " + usuario.getRol().getValor() + ".");
        return usuario;
    }
}
