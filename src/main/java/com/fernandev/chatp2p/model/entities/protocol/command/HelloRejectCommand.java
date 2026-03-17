package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.messages.HelloReject;
import com.fernandev.chatp2p.model.network.SocketClient;

public class HelloRejectCommand implements ProtocolCommand {
    @Override
    public void handle(SocketClient socketClient, MessageProtocol messageProtocol) {
        socketClient.setRejected(true);
        ConnectionController.getInstance().removeConnection(messageProtocol.getIp(), false);
    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {

    }
}
