package com.gimnasio.domain.converter;

import com.gimnasio.domain.enums.TipoNotificacion;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoNotificacionConverter implements AttributeConverter<TipoNotificacion, String> {

    @Override
    public String convertToDatabaseColumn(TipoNotificacion atributo) {
        return atributo == null ? null : atributo.getValor();
    }

    @Override
    public TipoNotificacion convertToEntityAttribute(String datos) {
        return datos == null ? null : TipoNotificacion.desdeValor(datos);
    }
}
