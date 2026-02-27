package com.fernandev.chatp2p.model.entities.command;

import java.util.regex.Pattern;

public class Hello extends MessageProtocol {
    public String idUser;

    public Hello(){
        super("004");
    }

    public Hello(String idUser) {
        super("004");
        this.idUser = idUser;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public static Hello parse(String message) {
        String[] split = message.split(Pattern.quote("|"));
        if (split.length != 2) throw new IllegalArgumentException("Formato de mensaje no válido");
        return new Hello(split[1]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + getIdUser() + System.lineSeparator();
    }
}
