package com.fernandev.chatp2p.model.entities.command;

import com.fernandev.chatp2p.model.network.SocketClient;

import java.util.regex.Pattern;

public class FijarMensaje extends MessageProtocol {
    public String idMessage;

    public FijarMensaje(String idMessage) {
        super("011");
        this.idMessage = idMessage;
    }

    public FijarMensaje(){
        super("011");
    }

    public String getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(String idMessage) {
        this.idMessage = idMessage;
    }


    public static FijarMensaje parse(String message) {
        String[] split = message.split(Pattern.quote("|"));
        if (split.length != 2) throw new IllegalArgumentException("Formato de mensaje no válido");
        return new FijarMensaje(split[1]);
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
