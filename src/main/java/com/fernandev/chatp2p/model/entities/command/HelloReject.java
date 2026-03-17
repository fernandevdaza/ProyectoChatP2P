package com.fernandev.chatp2p.model.entities.command;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.model.network.SocketClient;

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

    @Override
    public void execute(SocketClient client) {
        client.send(this);
        ConnectionController.getInstance().removeConnection(client.getPeerId(), false);
    }

    @Override
    public void onReceive(SocketClient client) {
        client.setRejected(true);
    }

}
