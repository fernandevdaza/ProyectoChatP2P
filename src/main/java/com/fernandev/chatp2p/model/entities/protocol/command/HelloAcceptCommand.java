package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.network.SocketClient;

public class HelloAcceptCommand implements ProtocolCommand {

    @Override
    public void handle(SocketClient socketClient, MessageProtocol messageProtocol) {

    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {

    }
}
