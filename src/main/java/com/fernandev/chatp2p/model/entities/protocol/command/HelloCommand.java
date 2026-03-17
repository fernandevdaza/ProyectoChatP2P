package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.messages.Hello;
import com.fernandev.chatp2p.model.entities.protocol.messages.HelloAccept;
import com.fernandev.chatp2p.model.entities.protocol.messages.HelloReject;
import com.fernandev.chatp2p.model.network.SocketClient;

public class HelloCommand implements ProtocolCommand {
    @Override
    public void handle(SocketClient socketClient, MessageProtocol messageProtocol) {
        String peerId = ((Hello) messageProtocol).getIdUser();
        socketClient.setPeerId(peerId);

        if (PeerController.getInstance().getPeerById(peerId) != null) {
            ConnectionController.getInstance().addConnection(peerId, socketClient);
            ConnectionController.getInstance().sendMessage(peerId, new HelloAccept());
        } else {
            ConnectionController.getInstance().addConnection(peerId, socketClient);
            ConnectionController.getInstance().sendMessage(peerId, new HelloReject());
            socketClient.setRejected(true);
        }

    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {

    }
}
