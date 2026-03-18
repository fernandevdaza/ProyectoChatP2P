package com.fernandev.chatp2p.model.network;

import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;

public interface SocketListener {
    void onMessageReceived(SocketClient socketClient, MessageProtocol messageProtocol);
    void onClientDisconnected(SocketClient socketClient);
}