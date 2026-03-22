package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.messages.Aceptar;
import com.fernandev.chatp2p.model.network.SocketClient;

public class AceptarCommand implements ProtocolCommand {
    @Override
    public void handle(SocketClient socketClient, MessageProtocol messageProtocol) {
        String peerId = ((Aceptar) messageProtocol).getIdUsuario();
        String nombre =  ((Aceptar) messageProtocol).getNombre();
        String ip =  messageProtocol.getIp();

        socketClient.setPeerId(peerId);

        ConnectionController.getInstance().removeConnection(ip, true);
        ConnectionController.getInstance().addConnection(peerId, socketClient);

        PeerController.getInstance().savePeer(ip, peerId, nombre, socketClient.getPort());

        String conversationId = MessageController.getInstance().createConversation();
        MessageController.getInstance().setPeerToConversation(conversationId, peerId);

        PeerController.getInstance().setPeerStatus(peerId, true);
    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {
        Peer me = PeerController.getInstance().getMyself();
        ((Aceptar) messageProtocol).setIdUsuario(me.getId());
        ((Aceptar) messageProtocol).setNombre(me.getDisplayName());
        socketClient.send(messageProtocol);

        PeerController.getInstance().savePeer(socketClient.getSocketIp(), socketClient.getPeerId(), socketClient.getDisplayName(), socketClient.getPort());

        String conversationId = MessageController.getInstance().createConversation();

        MessageController.getInstance().setPeerToConversation(conversationId, socketClient.getPeerId());
        PeerController.getInstance().setPeerStatus(socketClient.getPeerId(), true);

    }
}
