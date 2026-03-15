package com.fernandev.chatp2p.model.entities.command;

import com.fernandev.chatp2p.model.network.SocketClient;

import java.util.regex.Pattern;

public class MessageImage extends MessageProtocol {
    public String idUser;
    public String idMessage;
    public String base64Image;

    public MessageImage(String idUser, String idMessage, String base64Image) {
        super("021");
        this.idUser = idUser;
        this.idMessage = idMessage;
        this.base64Image = base64Image;
    }

    public MessageImage() {
        super("021");
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

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }

    public static MessageImage parse(String message) {
        String[] split = message.split(Pattern.quote("|"));
        if (split.length != 4)
            throw new IllegalArgumentException("Formato de mensaje de imagen no válido");
        return new MessageImage(split[1], split[2], split[3]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + getIdUser() + "|" + getIdMessage() + "|" + getBase64Image() + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) {
        client.send(this);
    }
}
