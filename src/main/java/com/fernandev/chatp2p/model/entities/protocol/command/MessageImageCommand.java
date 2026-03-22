package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.messages.Mensaje;
import com.fernandev.chatp2p.model.entities.protocol.messages.MessageImage;
import com.fernandev.chatp2p.model.network.SocketClient;

public class MessageImageCommand implements ProtocolCommand {
    @Override
    public void handle(SocketClient socketClient, MessageProtocol messageProtocol) {
        String peerId = ((MessageImage) messageProtocol).getIdUser();
        String messageId = ((MessageImage) messageProtocol).getIdMessage();
        String messageContent = ((MessageImage) messageProtocol).getBase64Image();
        String conversationId = MessageController.getInstance().getConversationIdByPeerId(peerId);
        MessageController.getInstance().saveMessage(messageId, conversationId, peerId,
                messageContent, false, true);
    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {
        Peer me = PeerController.getInstance().getMyself();
        String conversationId = MessageController.getInstance()
                .getConversationIdByPeerId(socketClient.getPeerId());

        messageProtocol.setIp(socketClient.getHostIp());

        ((MessageImage) messageProtocol).setIdUser(me.getId());

        MessageController.getInstance().saveMessage(((MessageImage) messageProtocol).getIdMessage(), conversationId, me.getId(), ((MessageImage) messageProtocol).getBase64Image(), false, true);

        socketClient.send(messageProtocol);
    }
}
