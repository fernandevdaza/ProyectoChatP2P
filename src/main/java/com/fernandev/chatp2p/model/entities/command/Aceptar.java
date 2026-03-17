package com.fernandev.chatp2p.model.entities.command;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.network.SocketClient;

import java.util.regex.Pattern;

public class Aceptar extends MessageProtocol {

    private String idUsuario;
    private String nombre;

    public Aceptar() {
        super("002");
    }
    public Aceptar(String idUsuario, String nombre) {
        super("002");
        this.idUsuario = idUsuario;
        this.nombre = nombre;
    }

    public static Aceptar parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 3) {
                                throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new Aceptar(split[1], split[2]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() +"|" +idUsuario +"|" +nombre + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) {
        Peer me = PeerController.getInstance().getMyself();
        this.setIdUsuario(me.getId());
        this.setNombre(me.getDisplayName());
        client.send(this);

        PeerController.getInstance().savePeer(client.getIp(), client.getPeerId(), client.getDisplayName(), client.getPort());

        String conversationId = MessageController.getInstance().createConversation();

        MessageController.getInstance().setPeerToConversation(conversationId, client.getPeerId());
    }

    @Override
    public void onReceive(SocketClient client) {
        client.setPeerId(this.getIdUsuario());
    }

    public String getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }




}
