package com.gimnasio.dto;

public record RecuperarContrasenaRequest(
        @jakarta.validation.constraints.NotBlank(message = "El correo es obligatorio")
        @jakarta.validation.constraints.Email(message = "Correo electrónico inválido")
        String email
) {
}
