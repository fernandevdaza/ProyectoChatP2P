package com.fernandev.chatp2p.model.network;

import com.fernandev.chatp2p.model.entities.command.MessageProtocol;

public interface SocketListener {
    void onMessageReceived(SocketClient client, MessageProtocol message);
    void onClientDisconnected(SocketClient client);
}