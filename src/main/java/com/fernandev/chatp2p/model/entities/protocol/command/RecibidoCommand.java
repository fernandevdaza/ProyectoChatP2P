package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.MessageStatusType;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.messages.Recibido;
import com.fernandev.chatp2p.model.network.SocketClient;

public class RecibidoCommand implements ProtocolCommand {
    @Override
    public void handle(SocketClient socketClient, MessageProtocol messageProtocol) {
        String messageId = ((Recibido) messageProtocol).getIdMessage();
        String peerId = messageProtocol.getIp() != null
                ? PeerController.getInstance().getPeerIdByIp(messageProtocol.getIp())
                : null;
        if (peerId != null) {
            MessageController.getInstance().updateMessageStatus(messageId, MessageStatusType.RECEIVED);
            MessageController.getInstance().saveReceipt(messageId, peerId);
        }
    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {
        messageProtocol.setIp(socketClient.getHostIp());
        socketClient.send(messageProtocol);
    }
}
