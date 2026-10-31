package com.gimnasio.domain.converter;

import com.gimnasio.domain.enums.TipoZona;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoZonaConverter implements AttributeConverter<TipoZona, String> {

    @Override
    public String convertToDatabaseColumn(TipoZona atributo) {
        return atributo == null ? null : atributo.getValor();
    }

    @Override
    public TipoZona convertToEntityAttribute(String datos) {
        return datos == null ? null : TipoZona.desdeValor(datos);
    }
}
