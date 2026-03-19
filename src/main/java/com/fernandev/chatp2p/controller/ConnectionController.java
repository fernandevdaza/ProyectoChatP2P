package com.fernandev.chatp2p.controller;

import com.fernandev.chatp2p.controller.exception.UnreachableException;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.factory.ProtocolCommandFactory;
import com.fernandev.chatp2p.model.entities.protocol.messages.*;
import com.fernandev.chatp2p.model.network.SocketClient;
import com.fernandev.chatp2p.model.network.SocketListener;
import com.fernandev.chatp2p.view.ChatUI;

import lombok.*;
import javax.swing.*;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ConnectionController implements SocketListener {
    private static final ConnectionController instance = new ConnectionController();
    private final Map<String, SocketClient> connections = new HashMap<>();
    private ChatUI ui;
    private PeerController peerController;
    private MessageController messageController;
    private int port;
    private boolean isOffline = false;

    public static ConnectionController getInstance() {
        return instance;
    }


    public boolean getOffline() {
        return this.isOffline;
    }


    public SocketClient connectToPeer(String ip) throws UnreachableException {
        try {
            SocketClient connection = new SocketClient(ip, port);
            connection.addListener(this);
            connection.setName("SocketClient-" + connection.getIp());
            connection.start();
            return connection;
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnreachableException("El cliente con ip: " + ip + " no está conectado.");
        }
    }

    public void sendMessage(String peerId, MessageProtocol messageProtocol) throws UnreachableException {
        SocketClient socketClient = connections.get(peerId);
        String ip = PeerController.getInstance().getPeerIpById(peerId);
        if (socketClient == null) {
            if (ip == null)
                ip = peerId;
            socketClient = this.connectToPeer(ip);
            connections.put(ip, socketClient);
        }
        if (socketClient.getPeerId() == null && ip != null) {
            socketClient.setPeerId(peerId);
        }
        ProtocolCommand protocolCommand = ProtocolCommandFactory.create(messageProtocol);
        protocolCommand.send(socketClient, messageProtocol);
//        messageProtocol.execute(socketClient);
    }


    public String getHostIpByPeerId(String peerId) {
        SocketClient socketClient = connections.get(peerId);
        if (socketClient != null) {
            return socketClient.getHostIp();
        }
        return null;
    }

    public void onModeOffline() throws SQLException, ConnectException {
        if (isOffline) {
            this.setOffline(false);
        } else {
            Peer me = peerController.getMyself();
            Offline offline = new Offline(me.getId());
            connections.forEach((id, socketClient) -> {
                this.sendMessage(id, offline);
            });
            this.setOffline(true);
        }
    }



    public void closeConnectionWithPeer(String id) {
        SocketClient socketClient = connections.get(id);
        if (socketClient != null) {
            socketClient.interrupt();
        }
    }

    public void addConnection(String peerId, SocketClient socketClient) {
        connections.put(peerId, socketClient);
    }

    public void removeConnection(String id, boolean disconnected) {
        if (!disconnected) {
            closeConnectionWithPeer(id);
        }
        connections.remove(id);
    }

    public void removeAllConnections() {
        for (String id : connections.keySet()) {
            this.removeConnection(id, false);
        }
        connections.clear();
    }

    public void shutdown() {
        this.removeAllConnections();
    }

    @Override
    public void onMessageReceived(SocketClient socketClient, MessageProtocol messageProtocol) {
        if (this.isOffline) {
            Peer me = peerController.getMyself();
            Offline offline = new Offline(me.getId());
            this.sendMessage(socketClient.getPeerId(), offline);
            return;
        }

        ProtocolCommand protocolCommand = ProtocolCommandFactory.create(messageProtocol);
        protocolCommand.handle(socketClient, messageProtocol);

        SwingUtilities.invokeLater(() -> {
            if (messageProtocol instanceof Invitacion) {

                    ui.onInvitationReceived(
                            ((Invitacion) messageProtocol).getIdUsuario(),
                            ((Invitacion) messageProtocol).getNombre()
                    );

            } else if (messageProtocol instanceof Aceptar) {

                    ui.onAcceptedReceived(
                            ((Aceptar) messageProtocol).getIdUsuario(),
                            ((Aceptar) messageProtocol).getNombre()
                    );

            } else if (messageProtocol instanceof Rechazar) {

                    ui.onRejectReceived(messageProtocol.getIp());

            } else if (messageProtocol instanceof Hello) {

                    ui.onHelloAcceptReceived(
                            ((Hello) messageProtocol).getIdUser(),
                            socketClient.isRejected()
                    );

            } else if (messageProtocol instanceof HelloAccept) {

                    ui.onHelloAcceptReceived(
                            ((HelloAccept) messageProtocol).getIdUser(),
                            socketClient.isRejected()
                    );

            } else if (messageProtocol instanceof HelloReject) {

                    ui.onHelloRejectReceived(messageProtocol.getIp());

            } else if (messageProtocol instanceof Mensaje) {

                ui.oneMessageReceived(
                        ((Mensaje) messageProtocol).getIdUser(),
                        ((Mensaje) messageProtocol).getIdMessage(),
                        ((Mensaje) messageProtocol).getMessage(),
                        false
                );

            } else if (messageProtocol instanceof Recibido) {
                ui.onReceiptReceived(
                        ((Recibido) messageProtocol).getIdMessage()
                );

            } else if (messageProtocol instanceof EliminarMensaje) {
                ui.onDeleteMessageReceived();
            }
            else if (messageProtocol instanceof Zumbido) {
                ui.onBuzz(
                        ((Zumbido) messageProtocol).getIdUser()
                );
            } else if (messageProtocol instanceof FijarMensaje) {

                ui.onPinMessageReceived(
                        true,
                        ((FijarMensaje) messageProtocol).getIdMessage()
                );

            } else if (messageProtocol instanceof MensajeUnico) {

                ui.oneMessageReceived(
                        ((MensajeUnico) messageProtocol).getIdUser(),
                        ((MensajeUnico) messageProtocol).getIdMessage(),
                        ((MensajeUnico) messageProtocol).getMessage(),
                        true
                );

            }else if (messageProtocol instanceof CambiarTema) {

                ui.onChangeThemeReceived(((CambiarTema) messageProtocol).getIdTema());

            } else if (messageProtocol instanceof Offline) {
                ui.onOfflineReceived(((Offline) messageProtocol).getIdUser());

            } else if (messageProtocol instanceof MessageImage) {
                ui.onImageReceived(((MessageImage) messageProtocol).getIdUser(), ((MessageImage) messageProtocol).getIdMessage(), ((MessageImage) messageProtocol).getBase64Image());
            }
        });

    }


    @Override
    public void onClientDisconnected(SocketClient socketClient) {
        Peer peer = peerController.getPeerByIp(socketClient.getIp());
        this.removeConnection(socketClient.getPeerId(), true);
        if (peer == null) {
            ui.onUnexpectedDisconnection(socketClient.getIp());
            return;
        }
        ui.updatePeerStatus(peer.getId(), false);
    }
}
