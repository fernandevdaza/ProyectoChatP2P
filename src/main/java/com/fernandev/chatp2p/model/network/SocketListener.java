package com.fernandev.chatp2p.model.network;

import com.fernandev.chatp2p.model.entities.command.MessageProtocol;

public interface SocketListener {
    void onMessage(SocketClient socketClient, MessageProtocol messageProtocol);
}
