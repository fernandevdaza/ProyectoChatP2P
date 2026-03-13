package com.fernandev.chatp2p.model.entities.command;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.network.SocketClient;

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

    @Override
    public void execute(SocketClient client) {
            Peer me = PeerController.getInstance().getMyself();
            this.setIdUser(me.getId());
            client.send(this);
            ConnectionController.getInstance().addConnection(client.getPeerId(), client);
    }
}