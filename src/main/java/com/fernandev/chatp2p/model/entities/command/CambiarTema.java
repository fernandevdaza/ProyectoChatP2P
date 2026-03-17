package com.fernandev.chatp2p.model.entities.command;

import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.network.SocketClient;

import java.util.regex.Pattern;

public class CambiarTema extends MessageProtocol {

    private String idUsuario;
    private String idTema;


    public CambiarTema() {
        super("013");
    }

    public CambiarTema(String idUsuario, String idTema) {
        super("013");
        this.idUsuario = idUsuario;
        this.idTema = idTema;
    }

    public static CambiarTema parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if (split.length != 3) {
            throw new IllegalArgumentException("Formato de trama CambiarTema inválido: " + trama);
        }
        CambiarTema ct = new CambiarTema(split[1], split[2]);
        return ct;
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + idUsuario + "|" + idTema + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) {
        Peer me = PeerController.getInstance().getMyself();
        if (me != null) {
            this.idUsuario = me.getId();
        }
        client.send(this);
    }

    @Override
    public void onReceive(SocketClient client) {

    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdTema() {
        return idTema;
    }

    public void setIdTema(String idTema) {
        this.idTema = idTema;
    }
}
