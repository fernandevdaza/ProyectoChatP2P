package com.fernandev.chatp2p.model.entities.command;

import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.network.SocketClient;
import com.fernandev.chatp2p.model.repository.PeerDao;

public class Offline extends MessageProtocol {
    public String idUser;

    public Offline(){
        super("0018");
    }

    public Offline(String idUser) {
        super("0018");
        this.idUser = idUser;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }


    public static Offline parse(String message) {
        String[] split = message.split("\\|");
        if (split.length != 2) throw new IllegalArgumentException("Formato de mensaje no válido");
        return new Offline(split[1]);
    }

    @Override
    public String generarTrama() {
        return  getCodigo() + "|" + getIdUser() + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) {
        Peer me = PeerDao.getInstance().findMe();
        this.setIdUser(me.getId());
        client.send(this);
    }
}