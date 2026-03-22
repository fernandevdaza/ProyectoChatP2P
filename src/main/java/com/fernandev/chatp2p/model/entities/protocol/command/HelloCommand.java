package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
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
        Peer peer = PeerController.getInstance().getPeerById(peerId);
        if (peer != null) {
            ConnectionController.getInstance().addConnection(peerId, socketClient);
            ConnectionController.getInstance().sendMessage(peerId, new HelloAccept());
            peer.setConnected(true);
        } else {
            ConnectionController.getInstance().addConnection(peerId, socketClient);
            ConnectionController.getInstance().sendMessage(peerId, new HelloReject());
            socketClient.setRejected(true);
        }

    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {
        Peer me = PeerController.getInstance().getMyself();
        String peerId = PeerController.getInstance().getPeerByIp(socketClient.getIp()) != null ? PeerController.getInstance().getPeerByIp(socketClient.getIp()).getId()
                : null;
        if (me == null || peerId == null || socketClient == null) {
            System.out.println("[" + Thread.currentThread().getName() + "]Hubo un problema al hacer HelloRequest");
            return;
        }
        ((Hello) messageProtocol).setIdUser(me.getId());
        socketClient.send(messageProtocol);
    }
}
