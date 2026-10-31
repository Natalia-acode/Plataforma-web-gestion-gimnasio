package com.gimnasio.domain.converter;

import com.gimnasio.domain.enums.EstadoSuscripcion;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoSuscripcionConverter implements AttributeConverter<EstadoSuscripcion, String> {

    @Override
    public String convertToDatabaseColumn(EstadoSuscripcion atributo) {
        return atributo == null ? null : atributo.getValor();
    }

    @Override
    public EstadoSuscripcion convertToEntityAttribute(String datos) {
        return datos == null ? null : EstadoSuscripcion.desdeValor(datos);
    }
}
