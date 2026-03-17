package com.fernandev.chatp2p.model.entities.protocol.parser;

import com.fernandev.chatp2p.model.entities.command.*;

import java.util.regex.Pattern;

public class ProtocolParser {
    public static MessageProtocol parse(String rawMessage){
        String[] split = rawMessage.split(Pattern.quote("|"));

        if (split.length < 1){
            throw new IllegalArgumentException("La trama está vacía");
        }

        if (!split[0].startsWith("0")){
            throw new IllegalArgumentException("Formato de trama no valido");
        }

        String code = split[0];

        return switch (code){
            case "001" -> Invitacion.parse(rawMessage);
            case "002" -> Aceptar.parse(rawMessage);
            case "003" -> Rechazar.parse(rawMessage);
            case "004" -> Hello.parse(rawMessage);
            case "005" -> HelloAccept.parse(rawMessage);
            case "006" -> HelloReject.parse(rawMessage);
            case "007" -> Mensaje.parse(rawMessage);
            case "008" -> Recibido.parse(rawMessage);
            case "009" -> EliminarMensaje.parse(rawMessage);
            case "010" -> Zumbido.parse(rawMessage);
            case "011" -> FijarMensaje.parse(rawMessage);
            case "012" -> MensajeUnico.parse(rawMessage);
            case "013" -> CambiarTema.parse(rawMessage);
            case "021" -> MessageImage.parse(rawMessage);
            default -> throw new IllegalArgumentException("Código inválido de trama");
        };
    }
}
