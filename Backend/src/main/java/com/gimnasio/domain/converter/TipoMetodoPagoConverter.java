package com.gimnasio.domain.converter;

import com.gimnasio.domain.enums.TipoMetodoPago;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoMetodoPagoConverter implements AttributeConverter<TipoMetodoPago, String> {

    @Override
    public String convertToDatabaseColumn(TipoMetodoPago atributo) {
        return atributo == null ? null : atributo.getValor();
    }

    @Override
    public TipoMetodoPago convertToEntityAttribute(String datos) {
        return datos == null ? null : TipoMetodoPago.desdeValor(datos);
    }
}
