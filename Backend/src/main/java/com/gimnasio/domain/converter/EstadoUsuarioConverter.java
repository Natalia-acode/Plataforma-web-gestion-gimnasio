package com.gimnasio.domain.converter;

import com.gimnasio.domain.enums.EstadoUsuario;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoUsuarioConverter implements AttributeConverter<EstadoUsuario, String> {

    @Override
    public String convertToDatabaseColumn(EstadoUsuario atributo) {
        return atributo == null ? null : atributo.getValor();
    }

    @Override
    public EstadoUsuario convertToEntityAttribute(String datos) {
        return datos == null ? null : EstadoUsuario.desdeValor(datos);
    }
}
