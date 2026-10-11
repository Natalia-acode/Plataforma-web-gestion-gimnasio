package com.gimnasio.exception;

/** Error de regla de negocio (RN-XX / EX de un caso de uso). */
public class BusinessException extends RuntimeException {

    public BusinessException(String mensaje) {
        super(mensaje);
    }
}
