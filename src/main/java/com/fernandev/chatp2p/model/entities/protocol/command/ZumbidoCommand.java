package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.messages.Zumbido;
import com.fernandev.chatp2p.model.network.SocketClient;

public class ZumbidoCommand implements ProtocolCommand {
    @Override
    public void handle(SocketClient socketClient, MessageProtocol messageProtocol) {

    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {
        Peer me = PeerController.getInstance().getMyself();
        String peerId = PeerController.getInstance().getPeerByIp(socketClient.getSocketIp()) != null ? PeerController.getInstance().getPeerByIp(socketClient.getSocketIp()).getId()
                : null;
        if (me == null || peerId == null || socketClient == null) {
            System.out.println("[" + Thread.currentThread().getName() + "]Hubo un problema al enviar el Zumbido");
            return;
        }
        ((Zumbido) messageProtocol).setIdUser(me.getId());
        socketClient.send(messageProtocol);
    }
}
