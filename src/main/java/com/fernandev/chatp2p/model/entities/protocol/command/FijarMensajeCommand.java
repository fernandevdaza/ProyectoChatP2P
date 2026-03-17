package com.fernandev.chatp2p.model.entities.protocol.command;

import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.messages.FijarMensaje;
import com.fernandev.chatp2p.model.network.SocketClient;

public class FijarMensajeCommand implements ProtocolCommand {
    @Override
    public void handle(SocketClient socketClient, MessageProtocol messageProtocol) {
        String messageId = ((FijarMensaje) messageProtocol).getIdMessage();
//        Message message = MessageController.getInstance().getMessageById(messageId);
//        boolean showPinMessage = message.getIsFixed();
//        MessageController.getInstance().pinMessage(messageId, !showPinMessage, false);
        MessageController.getInstance().pinMessage(messageId, true, false);
    }

    @Override
    public void send(SocketClient socketClient, MessageProtocol messageProtocol) {

    }
}
