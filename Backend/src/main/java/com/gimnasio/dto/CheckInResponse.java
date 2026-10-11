package com.gimnasio.dto;

public record CheckInResponse(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        String fechaHoraEntrada,
        String fechaHoraSalida,
        String metodo
) {
}
