package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.messages.Rechazar;
import com.fernandev.chatp2p.model.network.SocketClient;

public class RechazarCommand implements ProtocolCommand {
    @Override
    public void handle(SocketClient socketClient, MessageProtocol messageProtocol) {
        socketClient.setRejected(true);
        String ip = messageProtocol.getIp();
        ConnectionController.getInstance().removeConnection(ip, false);
    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {

    }
}
