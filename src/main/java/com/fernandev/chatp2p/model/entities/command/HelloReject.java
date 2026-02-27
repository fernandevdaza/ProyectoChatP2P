package com.fernandev.chatp2p.model.entities.command;

import java.util.regex.Pattern;

public class HelloReject extends MessageProtocol {
    public HelloReject() {
        super("006");
    }

    public static HelloReject parse(String message) {
        String[] split = message.split(Pattern.quote("|"));
        if (split.length != 1) throw new IllegalArgumentException("Formato de mensaje no válido");
        return new HelloReject();
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + System.lineSeparator();
    }

}
