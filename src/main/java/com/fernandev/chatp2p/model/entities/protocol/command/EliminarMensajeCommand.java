package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.messages.EliminarMensaje;
import com.fernandev.chatp2p.model.network.SocketClient;

public class EliminarMensajeCommand implements ProtocolCommand {
    @Override
    public void handle(SocketClient socketClient, MessageProtocol messageProtocol) {
        MessageController.getInstance().deleteMessage(((EliminarMensaje) messageProtocol).getIdMessage(), false);

    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {
        messageProtocol.setIp(socketClient.getHostIp());
        socketClient.send(messageProtocol);
    }
}
