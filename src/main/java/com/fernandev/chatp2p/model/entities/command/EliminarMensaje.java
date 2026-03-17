package com.fernandev.chatp2p.model.entities.command;

import com.fernandev.chatp2p.model.network.SocketClient;

import java.util.regex.Pattern;

public class EliminarMensaje extends MessageProtocol {
    public String idMessage;

    public EliminarMensaje(String idMessage) {
        super("009");
        this.idMessage = idMessage;
    }

    public EliminarMensaje(){
        super("009");
    }

    public String getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(String idMessage) {
        this.idMessage = idMessage;
    }


    public static EliminarMensaje parse(String message) {
        String[] split = message.split(Pattern.quote("|"));
        if (split.length != 2) throw new IllegalArgumentException("Formato de mensaje no válido");
        return new EliminarMensaje(split[1]);
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

    @Override
    public void onReceive(SocketClient client) {

    }
}
