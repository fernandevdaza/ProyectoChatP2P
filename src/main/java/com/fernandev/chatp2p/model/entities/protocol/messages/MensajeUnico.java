package com.fernandev.chatp2p.model.entities.protocol.messages;

import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.network.SocketClient;

import java.util.regex.Pattern;

public class MensajeUnico extends MessageProtocol {
    public String idUser;
    public String idMessage;
    public String message;

    public MensajeUnico(String idUser, String idMessage, String message) {
        super("012");
        this.idUser = idUser;
        this.idMessage = idMessage;
        this.message = message;
    }

    public MensajeUnico(){
        super("012");
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(String idMessage) {
        this.idMessage = idMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static MensajeUnico parse(String message) {
        String[] split = message.split(Pattern.quote("|"));
        if (split.length != 4) throw new IllegalArgumentException("Formato de mensaje no válido");
        return new MensajeUnico(split[1], split[2], split[3]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + getIdUser() + "|" + getIdMessage() + "|" + getMessage() + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) {
        Peer me = PeerController.getInstance().getMyself();
        String conversationId = MessageController.getInstance()
                .getConversationIdByPeerId(client.getPeerId());

        this.setIp(client.getHostIp());

        this.setIdUser(me.getId());

        MessageController.getInstance().saveMessage(this.getIdMessage(), conversationId, me.getId(), this.getMessage(), true, false);

        client.send(this);
    }

    @Override
    public void onReceive(SocketClient client) {

    }
}

