package com.gimnasio.domain.converter;

import com.gimnasio.domain.enums.MetodoCheckIn;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MetodoCheckInConverter implements AttributeConverter<MetodoCheckIn, String> {

    @Override
    public String convertToDatabaseColumn(MetodoCheckIn atributo) {
        return atributo == null ? null : atributo.getValor();
    }

    @Override
    public MetodoCheckIn convertToEntityAttribute(String datos) {
        return datos == null ? null : MetodoCheckIn.desdeValor(datos);
    }
}
