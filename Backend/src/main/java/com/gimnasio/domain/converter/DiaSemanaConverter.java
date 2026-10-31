package com.gimnasio.domain.converter;

import com.gimnasio.domain.enums.DiaSemana;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DiaSemanaConverter implements AttributeConverter<DiaSemana, String> {

    @Override
    public String convertToDatabaseColumn(DiaSemana atributo) {
        return atributo == null ? null : atributo.getValor();
    }

    @Override
    public DiaSemana convertToEntityAttribute(String datos) {
        return datos == null ? null : DiaSemana.desdeValor(datos);
    }
}
