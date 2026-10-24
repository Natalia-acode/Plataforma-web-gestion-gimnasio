package com.gimnasio.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.enums.EstadoUsuario;
import com.gimnasio.domain.enums.Rol;
import com.gimnasio.dto.LoginRequest;
import com.gimnasio.dto.LoginResponse;
import com.gimnasio.dto.RegistroRequest;
import com.gimnasio.exception.BusinessException;
import com.gimnasio.repository.UsuarioRepository;
import com.gimnasio.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    void registrarCliente_exito() {
        RegistroRequest solicitud = new RegistroRequest(
                "Juan", "Pérez", "juan@email.com", "Pass1234!", "3001112222", null);

        when(usuarioRepository.existsByEmail("juan@email.com")).thenReturn(false);
        when(passwordEncoder.encode("Pass1234!")).thenReturn("bcrypt-hash");
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("token-jwt");

        LoginResponse respuesta = authService.registrarCliente(solicitud);

        assertNotNull(respuesta);
        assertEquals("token-jwt", respuesta.token());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void registrarCliente_emailDuplicado_lanzaExcepcion() {
        RegistroRequest solicitud = new RegistroRequest(
                "Juan", "Pérez", "juan@email.com", "Pass1234!", null, null);

        when(usuarioRepository.existsByEmail("juan@email.com")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> authService.registrarCliente(solicitud));
        verify(usuarioRepository, never()).save(any(Usuario.class));
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
}
