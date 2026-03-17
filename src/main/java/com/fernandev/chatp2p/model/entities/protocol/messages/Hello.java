package com.fernandev.chatp2p.model.entities.protocol.messages;

import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.network.SocketClient;

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

    @Override
    public void execute(SocketClient client) {
        Peer me = PeerController.getInstance().getMyself();
        String peerId = PeerController.getInstance().getPeerByIp(client.getIp()) != null ? PeerController.getInstance().getPeerByIp(client.getIp()).getId()
                : null;
        if (me == null || peerId == null || client == null) {
            System.out.println("[" + Thread.currentThread().getName() + "]Hubo un problema al hacer HelloRequest");
            return;
        }
        this.setIdUser(me.getId());
        client.send(this);
    }

    @Override
    public void onReceive(SocketClient client) {
        client.setPeerId(this.getIdUser());
    }
}
