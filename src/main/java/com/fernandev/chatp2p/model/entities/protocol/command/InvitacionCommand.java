package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.messages.Invitacion;
import com.fernandev.chatp2p.model.network.NetworkUtils;
import com.fernandev.chatp2p.model.network.SocketClient;

public class InvitacionCommand implements ProtocolCommand {


    @Override
    public void handle(SocketClient socketClient, MessageProtocol messageProtocol) {
        socketClient.setPeerId(((Invitacion) messageProtocol).getIdUsuario());
        socketClient.setDisplayName(((Invitacion) messageProtocol).getNombre());

        String peerId = ((Invitacion) messageProtocol).getIdUsuario();

        ConnectionController.getInstance().addConnection(peerId, socketClient);
    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {
        Peer me = PeerController.getInstance().getMyself();
        ((Invitacion) messageProtocol).setIdUsuario(me.getId());
        ((Invitacion) messageProtocol).setNombre(me.getDisplayName());
        socketClient.send(messageProtocol);
    }
}
