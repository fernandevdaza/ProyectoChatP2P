package com.fernandev.chatp2p.model.entities.command;

import com.fernandev.chatp2p.model.network.SocketClient;

import java.util.regex.Pattern;

public class Mensaje extends MessageProtocol {
    public String idUser;
    public String idMessage;
    public String message;

    public Mensaje(String idUser, String idMessage, String message) {
        super("007");
        this.idUser = idUser;
        this.idMessage = idMessage;
        this.message = message;
    }

    public Mensaje(){
        super("007");
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

    public static Mensaje parse(String message) {
        String[] split = message.split(Pattern.quote("|"));
        if (split.length != 4) throw new IllegalArgumentException("Formato de mensaje no válido");
        return new Mensaje(split[1], split[2], split[3]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + getIdUser() + "|" + getIdMessage() + "|" + getMessage() + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) {
        client.send(this);
    }
}
