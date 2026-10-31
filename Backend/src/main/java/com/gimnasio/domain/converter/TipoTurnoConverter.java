package com.gimnasio.domain.converter;

import com.gimnasio.domain.enums.TipoTurno;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoTurnoConverter implements AttributeConverter<TipoTurno, String> {

    @Override
    public String convertToDatabaseColumn(TipoTurno atributo) {
        return atributo == null ? null : atributo.getValor();
    }

    @Override
    public TipoTurno convertToEntityAttribute(String datos) {
        return datos == null ? null : TipoTurno.desdeValor(datos);
    }
}
