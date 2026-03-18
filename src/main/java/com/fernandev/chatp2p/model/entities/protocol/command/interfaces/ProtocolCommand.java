package com.fernandev.chatp2p.model.entities.protocol.command.interfaces;

import com.fernandev.chatp2p.model.network.SocketClient;

public interface ProtocolCommand {
    void handle(SocketClient socketClient, MessageProtocol messageProtocol);
    void send(SocketClient socketClient, MessageProtocol messageProtocol);
}
