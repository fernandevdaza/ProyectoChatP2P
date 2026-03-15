package com.fernandev.chatp2p.model.entities.command;

import com.fernandev.chatp2p.controller.MessageController;
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
    public CambiarTema(String idUsuario, String nombre) {
        super("013");
        this.idUsuario = idUsuario;
        this.idTema = idTema;
    }

    public static CambiarTema parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 3) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new CambiarTema(split[1], split[2]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() +"|" +idUsuario +"|" + idTema + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) {
        Peer me = PeerController.getInstance().getMyself();
        this.setIdUsuario(me.getId());
        this.setIdTema(me.getDisplayName());
        client.send(this);

        PeerController.getInstance().savePeer(client.getIp(), client.getPeerId(), client.getDisplayName(), client.getPort());

        String conversationId = MessageController.getInstance().createConversation();

        MessageController.getInstance().setPeerToConversation(conversationId, client.getPeerId());
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
    public void setIdTema(String nombre) {
        this.idTema = idTema;
    }




}
