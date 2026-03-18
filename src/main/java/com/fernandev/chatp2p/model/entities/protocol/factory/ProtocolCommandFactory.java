package com.fernandev.chatp2p.model.entities.protocol.factory;

import com.fernandev.chatp2p.model.entities.protocol.command.*;
import com.fernandev.chatp2p.model.entities.protocol.command.HelloRejectCommand;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.messages.*;

public class ProtocolCommandFactory {

    public static ProtocolCommand create(MessageProtocol messageProtocol){
        return switch (messageProtocol.getCodigo()){
            case "001" -> new InvitacionCommand();
            case "002" -> new AceptarCommand();
            case "003" -> new RechazarCommand();
            case "004" -> new HelloCommand();
            case "005" -> new HelloAcceptCommand();
            case "006" -> new HelloRejectCommand();
            case "007" -> new MensajeCommand();
            case "008" -> new RecibidoCommand();
            case "009" -> new EliminarMensajeCommand();
            case "010" -> new ZumbidoCommand();
            case "011" -> new FijarMensajeCommand();
            case "012" -> new MensajeUnicoCommand();
            case "013" -> new CambiarTemaCommand();
            case "021" -> new MessageImageCommand();
            default -> throw new IllegalArgumentException("Código inválido de trama");
        };
    }
}
