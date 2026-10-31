package com.gimnasio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.enums.EstadoUsuario;
import com.gimnasio.domain.enums.Rol;
import com.gimnasio.domain.enums.TipoNotificacion;
import com.gimnasio.dto.LoginRequest;
import com.gimnasio.dto.LoginResponse;
import com.gimnasio.dto.RegistroRequest;
import com.gimnasio.dto.RegistroResponse;
import com.gimnasio.exception.BusinessException;
import com.gimnasio.repository.UsuarioRepository;
import com.gimnasio.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private NotificacionService notificacionService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                usuarioRepository, passwordEncoder, jwtService, notificacionService);
    }

    @Test
    void registrar_cliente_naceActivaConToken() {
        RegistroRequest solicitud = new RegistroRequest(
                "Juan", "Pérez", "juan@email.com", "Pass1234!", "3001112222", null, null);

        when(usuarioRepository.existsByEmail("juan@email.com")).thenReturn(false);
        when(passwordEncoder.encode("Pass1234!")).thenReturn("bcrypt-hash");
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("token-jwt");

        RegistroResponse respuesta = authService.registrar(solicitud);

        assertNotNull(respuesta);
        assertFalse(respuesta.pendienteDeAprobacion());
        assertEquals("token-jwt", respuesta.token());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void registrar_emailDuplicado_lanzaExcepcion() {
        RegistroRequest solicitud = new RegistroRequest(
                "Juan", "Pérez", "juan@email.com", "Pass1234!", null, null, null);

        when(usuarioRepository.existsByEmail("juan@email.com")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> authService.registrar(solicitud));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void arranque_primerAdministrador_seAutoactiva() {
        RegistroRequest solicitud = new RegistroRequest(
                "Ana", "López", "ana@admin.com", "Pass1234!", null, null, "administrador");

        when(usuarioRepository.existsByEmail("ana@admin.com")).thenReturn(false);
        when(usuarioRepository.findByRolAndEstado(Rol.ADMINISTRADOR, EstadoUsuario.ACTIVO)).thenReturn(List.of());
        when(passwordEncoder.encode("Pass1234!")).thenReturn("bcrypt-hash");
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("token-jwt");

        RegistroResponse respuesta = authService.registrar(solicitud);

        assertFalse(respuesta.pendienteDeAprobacion());
        assertEquals("administrador", respuesta.usuario().rol().getValor());
        assertEquals("token-jwt", respuesta.token());
    }

    @Test
    void registrar_nombreConNumeros_lanzaExcepcion() {
        RegistroRequest solicitud = new RegistroRequest(
                "Juan2", "Pérez", "juan2@email.com", "Pass1234!", null, null, null);

        when(usuarioRepository.existsByEmail("juan2@email.com")).thenReturn(false);

        assertThrows(BusinessException.class, () -> authService.registrar(solicitud));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void registrar_apellidosConNumeros_lanzaExcepcion() {
        RegistroRequest solicitud = new RegistroRequest(
                "Juan", "Pérez2", "juan3@email.com", "Pass1234!", null, null, null);

        when(usuarioRepository.existsByEmail("juan3@email.com")).thenReturn(false);

        assertThrows(BusinessException.class, () -> authService.registrar(solicitud));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void registrarPersonal_conAdministradorActivo_quedaPendienteSinToken() {
        Usuario administrador = new Usuario();
        administrador.setId(1L);
        administrador.setNombre("Ana");
        administrador.setApellidos("López");
        administrador.setEmail("ana@admin.com");
        administrador.setRol(Rol.ADMINISTRADOR);
        administrador.setEstado(EstadoUsuario.ACTIVO);

        RegistroRequest solicitud = new RegistroRequest(
                "Pedro", "Ramírez", "pedro@email.com", "Pass1234!", null, null, "entrenador");

        when(usuarioRepository.existsByEmail("pedro@email.com")).thenReturn(false);
        when(usuarioRepository.findByRolAndEstado(Rol.ADMINISTRADOR, EstadoUsuario.ACTIVO))
                .thenReturn(List.of(administrador));
        when(passwordEncoder.encode("Pass1234!")).thenReturn("bcrypt-hash");

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        RegistroResponse respuesta = authService.registrar(solicitud);

        verify(usuarioRepository).save(guardado.capture());
        assertEquals(EstadoUsuario.PENDIENTE, guardado.getValue().getEstado());
        assertTrue(respuesta.pendienteDeAprobacion());
        assertNull(respuesta.token());
        verify(jwtService, never()).generarToken(any(Usuario.class));
        verify(notificacionService).enviar(eq(administrador), any(TipoNotificacion.class), anyString());
    }

    @Test
    void login_cuentaPendiente_lanzaExcepcion() {
        Usuario usuario = new Usuario();
        usuario.setEmail("pedro@email.com");
        usuario.setPasswordHash("bcrypt-hash");
        usuario.setRol(Rol.ENTRENADOR);
        usuario.setEstado(EstadoUsuario.PENDIENTE);

        when(usuarioRepository.findByEmail("pedro@email.com")).thenReturn(java.util.Optional.of(usuario));
        when(passwordEncoder.matches("Pass1234!", "bcrypt-hash")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> authService.login(new LoginRequest("pedro@email.com", "Pass1234!")));
    }

    @Test
    void login_cuentaRechazada_lanzaExcepcion() {
        Usuario usuario = new Usuario();
        usuario.setEmail("pedro@email.com");
        usuario.setPasswordHash("bcrypt-hash");
        usuario.setRol(Rol.RECEPCIONISTA);
        usuario.setEstado(EstadoUsuario.RECHAZADO);

        when(usuarioRepository.findByEmail("pedro@email.com")).thenReturn(java.util.Optional.of(usuario));
        when(passwordEncoder.matches("Pass1234!", "bcrypt-hash")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> authService.login(new LoginRequest("pedro@email.com", "Pass1234!")));
    }

    @Test
    void login_exito_devuelveToken() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("juan@email.com");
        usuario.setPasswordHash("bcrypt-hash");
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setRol(Rol.CLIENTE);

        LoginRequest solicitud = new LoginRequest("juan@email.com", "Pass1234!");

        when(usuarioRepository.findByEmail("juan@email.com")).thenReturn(java.util.Optional.of(usuario));
        when(passwordEncoder.matches("Pass1234!", "bcrypt-hash")).thenReturn(true);
        when(jwtService.generarToken(usuario)).thenReturn("token-jwt");

        LoginResponse respuesta = authService.login(solicitud);

        assertNotNull(respuesta);
        assertEquals("token-jwt", respuesta.token());
    }

    @Test
    void login_contrasenaIncorrecta_lanzaExcepcion() {
        Usuario usuario = new Usuario();
        usuario.setEmail("juan@email.com");
        usuario.setPasswordHash("bcrypt-hash");
        usuario.setEstado(EstadoUsuario.ACTIVO);

        LoginRequest solicitud = new LoginRequest("juan@email.com", "malacontraseña");

        when(usuarioRepository.findByEmail("juan@email.com")).thenReturn(java.util.Optional.of(usuario));
        when(passwordEncoder.matches("malacontraseña", "bcrypt-hash")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(solicitud));
    }

    @Test
    void login_cuentaSuspendida_lanzaExcepcion() {
        Usuario usuario = new Usuario();
        usuario.setEmail("juan@email.com");
        usuario.setPasswordHash("bcrypt-hash");
        usuario.setEstado(EstadoUsuario.SUSPENDIDO);

        LoginRequest solicitud = new LoginRequest("juan@email.com", "Pass1234!");

        when(usuarioRepository.findByEmail("juan@email.com")).thenReturn(java.util.Optional.of(usuario));
        when(passwordEncoder.matches("Pass1234!", "bcrypt-hash")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.login(solicitud));
    }

    @Test
    void recuperarContrasena_usuarioExiste_generaTokenYNotificacion() {
        Usuario usuario = new Usuario();
        usuario.setEmail("juan@email.com");
        when(usuarioRepository.findByEmail("juan@email.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        authService.recuperarContrasena("juan@email.com");

        assertNotNull(usuario.getPasswordResetToken());
        assertNotNull(usuario.getPasswordResetExpira());
        assertTrue(usuario.getPasswordResetExpira().isAfter(LocalDateTime.now()));
        verify(usuarioRepository).save(usuario);
        verify(notificacionService).enviar(eq(usuario), eq(TipoNotificacion.CUENTA), anyString());
    }

    @Test
    void restablecerContrasena_tokenValido_actualizaPassword() {
        Usuario usuario = new Usuario();
        usuario.setEmail("juan@email.com");
        usuario.setPasswordResetToken("token-valido");
        usuario.setPasswordResetExpira(LocalDateTime.now().plusMinutes(5));

        when(usuarioRepository.findByPasswordResetToken("token-valido")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("NuevaPass123!")).thenReturn("new-hash");

        authService.restablecerContrasena(new com.gimnasio.dto.RestablecerContrasenaRequest("token-valido", "NuevaPass123!"));

        assertNull(usuario.getPasswordResetToken());
        assertNull(usuario.getPasswordResetExpira());
        assertEquals("new-hash", usuario.getPasswordHash());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void actualizarPerfil_nombreConNumeros_lanzaExcepcion() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Ana");
        usuario.setApellidos("López");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThrows(BusinessException.class,
                () -> authService.actualizarPerfil(1L, new com.gimnasio.dto.PerfilRequest("Ana2", "López", null, null)));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
