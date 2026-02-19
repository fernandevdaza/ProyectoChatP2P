package edu.upb.chatupb_v2.bl.message;

import java.util.regex.Pattern;

public class HelloAccept extends MessageProtocol {
    public String idUser;

    public HelloAccept(String idUser) {
        super("005");
        this.idUser = idUser;
    }

    public HelloAccept() {
        super("005");
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public static HelloAccept parse(String message) {
        String[] split = message.split(Pattern.quote("|"));
        if (split.length != 2) throw new IllegalArgumentException("Formato de mensaje no válido");
        return new HelloAccept(split[1]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + getIdUser() + System.lineSeparator();
    }
}