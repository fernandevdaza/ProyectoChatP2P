package com.fernandev.chatp2p.model.entities.command;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.model.network.SocketClient;

public class Rechazar extends MessageProtocol {

    public Rechazar(){
        super("003");
    }

    public static Rechazar parse(String message) {
        String[] split = message.split("\\|");
        if (split.length != 1) throw new IllegalArgumentException("Formato de mensaje no válido");
        return new Rechazar();
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) {
        client.send(this);
        ConnectionController.getInstance().removeConnection(client.getPeerId(), true);
        if(!client.isClosed()){
            client.close();
        }
    }
}
