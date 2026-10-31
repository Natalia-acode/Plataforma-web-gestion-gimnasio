package com.gimnasio.domain.converter;

import com.gimnasio.domain.enums.EstadoTurno;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoTurnoConverter implements AttributeConverter<EstadoTurno, String> {

    @Override
    public String convertToDatabaseColumn(EstadoTurno atributo) {
        return atributo == null ? null : atributo.getValor();
    }

    @Override
    public EstadoTurno convertToEntityAttribute(String datos) {
        return datos == null ? null : EstadoTurno.desdeValor(datos);
    }
}
