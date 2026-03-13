package com.fernandev.chatp2p.model.entities.command;

import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.model.network.SocketClient;

import java.util.regex.Pattern;

public class Recibido extends MessageProtocol {
    public String idMessage;

    public Recibido(String idMessage) {
        super("008");
        this.idMessage = idMessage;
    }

    public Recibido(){
        super("008");
    }

    public String getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(String idMessage) {
        this.idMessage = idMessage;
    }


    public static Recibido parse(String message) {
        String[] split = message.split(Pattern.quote("|"));
        if (split.length != 2) throw new IllegalArgumentException("Formato de mensaje no válido");
        return new Recibido(split[1]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + getIdMessage() + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) {
        this.setIp(client.getHostIp());
        client.send(this);
    }
}
