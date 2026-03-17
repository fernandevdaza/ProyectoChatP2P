package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.messages.HelloAccept;
import com.fernandev.chatp2p.model.network.SocketClient;

public class HelloAcceptCommand implements ProtocolCommand {

    @Override
    public void handle(SocketClient socketClient, MessageProtocol messageProtocol) {
        String peerId = ((HelloAccept) messageProtocol).getIdUser();
        ConnectionController.getInstance().removeConnection(messageProtocol.getIp(), true);
        ConnectionController.getInstance().addConnection(peerId, socketClient);
    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {
        Peer me = PeerController.getInstance().getMyself();
        ((HelloAccept) messageProtocol).setIdUser(me.getId());
        socketClient.send(messageProtocol);
    }
}
