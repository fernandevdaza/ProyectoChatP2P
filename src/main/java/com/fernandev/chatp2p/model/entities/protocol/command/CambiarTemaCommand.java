package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.messages.CambiarTema;
import com.fernandev.chatp2p.model.network.SocketClient;

public class CambiarTemaCommand implements ProtocolCommand {
    @Override
    public void handle(SocketClient socketClient, MessageProtocol messageProtocol) {

    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {
        Peer me = PeerController.getInstance().getMyself();
        if (me != null) {
            ((CambiarTema) messageProtocol).setIdUsuario(me.getId());;
        }
        socketClient.send(messageProtocol);
    }
}
