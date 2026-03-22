package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.messages.MensajeUnico;
import com.fernandev.chatp2p.model.network.SocketClient;

public class MensajeUnicoCommand implements ProtocolCommand {
    @Override
    public void handle(SocketClient socketClient, MessageProtocol messageProtocol) {
        String userId = ((MensajeUnico) messageProtocol).getIdUser();
        String messageId = ((MensajeUnico) messageProtocol).getIdMessage();
        String message = ((MensajeUnico) messageProtocol).getMessage();

        String conversationId = MessageController.getInstance().getConversationIdByPeerId(userId);
        MessageController.getInstance().saveMessage(messageId, conversationId, userId,
                message, true, false);
    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {
        Peer me = PeerController.getInstance().getMyself();
        String conversationId = MessageController.getInstance()
                .getConversationIdByPeerId(socketClient.getPeerId());

        messageProtocol.setIp(socketClient.getHostIp());

        ((MensajeUnico) messageProtocol).setIdUser(me.getId());

        MessageController.getInstance().saveMessage(((MensajeUnico) messageProtocol).getIdMessage(), conversationId, me.getId(), "", true, false);

        socketClient.send(messageProtocol);
    }
}
