package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.MessageStatusType;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.messages.Mensaje;
import com.fernandev.chatp2p.model.entities.protocol.messages.Recibido;
import com.fernandev.chatp2p.model.network.SocketClient;

import java.util.Objects;

public class MensajeCommand implements ProtocolCommand {
    @Override
    public void handle(SocketClient socketClient, MessageProtocol messageProtocol) {
        try {
            String peerId = ((Mensaje) messageProtocol).getIdUser();
            String messageId = ((Mensaje) messageProtocol).getIdMessage();
            String messageContent = ((Mensaje) messageProtocol).getMessage();
            String conversationId = MessageController.getInstance().getConversationIdByPeerId(peerId);
            MessageController.getInstance().saveMessage(messageId, conversationId, peerId,
                    messageContent, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {
        Peer me = PeerController.getInstance().getMyself();
        String conversationId = MessageController.getInstance()
                .getConversationIdByPeerId(socketClient.getPeerId());

        messageProtocol.setIp(socketClient.getHostIp());

        ((Mensaje) messageProtocol).setIdUser(me.getId());

        MessageController.getInstance().saveMessage(((Mensaje) messageProtocol).getIdMessage(), conversationId, me.getId(), ((Mensaje) messageProtocol).getMessage(), false);

        socketClient.send(messageProtocol);
    }
}
