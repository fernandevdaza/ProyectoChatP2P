package com.fernandev.chatp2p.controller;

import com.fernandev.chatp2p.controller.exception.UnreachableException;
import com.fernandev.chatp2p.model.entities.command.*;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.network.SocketClient;
import com.fernandev.chatp2p.model.network.SocketListener;
import com.fernandev.chatp2p.model.repository.PeerDao;
import com.fernandev.chatp2p.view.ChatUI;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

    public void setUI(ChatUI ui) {
        this.ui = ui;
    }

    public void setPeerController(PeerController peerController) {
        this.peerController = peerController;
    }

    public void setMessageController(MessageController messageController) {
        this.messageController = messageController;
    }

    private SocketClient connectToPeer(String ip) throws UnreachableException {
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

    public void connectAndSendInvitation(String ip, String myId, String myName) throws UnreachableException {
        SocketClient socketClient = connectToPeer(ip);
        connections.put(ip, socketClient);
        Invitacion invitacion = new Invitacion(myId, myName);
        sendMessageInternal(invitacion, socketClient);
    }

    public void sendMessageById(String peerId, MessageProtocol messageProtocol) {
        SocketClient socketClient = connections.get(peerId);
        if (socketClient != null) {
            sendMessageInternal(messageProtocol, socketClient);
        } else {
            System.out.println("[CONN] No hay conexión para peer: " + peerId);
        }
    }

    public String getHostIpByPeerId(String peerId) {
        SocketClient socketClient = connections.get(peerId);
        if (socketClient != null) {
            return socketClient.getHostIp();
        }
        return null;
    }

    public int getPortByPeerId(String peerId) {
        SocketClient socketClient = connections.get(peerId);
        if (socketClient != null) {
            return socketClient.getPort();
        }
        return 0;
    }

    private void sendMessageInternal(MessageProtocol messageProtocol, SocketClient socketClient) {
        try {
            socketClient.send(messageProtocol);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendHelloToPeer(String ip) {
        try {
            Peer me = PeerDao.getInstance().findMe();
            String peerId = PeerDao.getInstance().findByIp(ip) != null ? PeerDao.getInstance().findByIp(ip).getId()
                    : null;
            SocketClient socketClient = this.connectToPeer(ip);
            if (me == null || peerId == null || socketClient == null) {
                System.out.println("[" + Thread.currentThread().getName() + "]Hubo un problema al hacer HelloRequest");
                return;
            }
            MessageProtocol hello = new Hello(me.getId());
            this.sendMessageInternal(hello, socketClient);
        } catch (UnreachableException ue) {
            ue.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onModeOffline() throws SQLException, ConnectException {
        if (isOffline) {
            this.setOffline(false);
        } else {
            Peer me = PeerDao.getInstance().findMe();
            Offline offline = new Offline(me.getId());
            connections.forEach((id, socketClient) -> {
                this.sendMessageInternal(offline, socketClient);
            });
            this.setOffline(true);
        }
    }

    public boolean getOffline() {
        return this.isOffline;
    }

    public void setOffline(boolean isOffline) {
        this.isOffline = isOffline;
    }

    public void closeConnectionWithPeer(String id) {
        SocketClient socketClient = connections.get(id);
        if (socketClient != null) {
            socketClient.interrupt();
        }
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
            Peer me = PeerDao.getInstance().findMe();
            Offline offline = new Offline(me.getId());
            this.sendMessageInternal(offline, socketClient);
            return;
        }

        if (messageProtocol instanceof Invitacion) {
            handleInvitacion(socketClient, (Invitacion) messageProtocol);
        } else if (messageProtocol instanceof Aceptar) {
            handleAceptar(socketClient, (Aceptar) messageProtocol);
        } else if (messageProtocol instanceof Rechazar) {
            handleRechazar(socketClient, (Rechazar) messageProtocol);
        } else if (messageProtocol instanceof Mensaje) {
            handleMensaje((Mensaje) messageProtocol);
        } else if (messageProtocol instanceof Hello) {
            handleHello(socketClient, (Hello) messageProtocol);
        } else if (messageProtocol instanceof HelloAccept) {
            handleHelloAccept(socketClient, (HelloAccept) messageProtocol);
        } else if (messageProtocol instanceof HelloReject) {
            handleHelloReject((HelloReject) messageProtocol);
        } else if (messageProtocol instanceof Recibido) {
            handleRecibido((Recibido) messageProtocol);
        } else if (messageProtocol instanceof Offline) {
            handleOffline((Offline) messageProtocol);
        }
    }

    private void handleInvitacion(SocketClient socketClient, Invitacion invitacion) {
        String peerId = invitacion.getIdUsuario();
        String nombre = invitacion.getNombre();
        String ip = invitacion.getIp();

        connections.put(peerId, socketClient);

        boolean accepted = ui.onInvitationReceived(peerId, nombre);

        if (accepted) {
            try {
                Peer me = peerController.getMyself();
                Aceptar aceptar = new Aceptar(me.getId(), me.getDisplayName());
                sendMessageInternal(aceptar, socketClient);

                peerController.savePeer(ip, peerId, nombre, port);

                String conversationId = messageController.createConversation();
                messageController.setPeerToConversation(conversationId, peerId);

                ui.onInvitationAccepted(peerId, nombre, ip, socketClient.getPort());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            Rechazar rechazar = new Rechazar();
            sendMessageInternal(rechazar, socketClient);
            socketClient.close();
            connections.remove(peerId);
        }
    }

    private void handleAceptar(SocketClient socketClient, Aceptar aceptar) {
        try {
            String peerId = aceptar.getIdUsuario();
            String nombre = aceptar.getNombre();
            String ip = aceptar.getIp();

            connections.remove(ip);
            connections.put(peerId, socketClient);

            peerController.savePeer(ip, peerId, nombre, port);

            String conversationId = messageController.createConversation();
            messageController.setPeerToConversation(conversationId, peerId);

            ui.onInvitationAccepted(peerId, nombre, ip, socketClient.getPort());

            javax.swing.SwingUtilities.invokeLater(() -> javax.swing.JOptionPane.showMessageDialog(ui,
                    nombre + " aceptó la conexión."));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRechazar(SocketClient socketClient, Rechazar rechazar) {
        String ip = rechazar.getIp();
        connections.remove(ip);
        socketClient.close();
        ui.onInvitationRejected(ip);
    }

    private void handleMensaje(Mensaje msg) {
        try {
            String conversationId = messageController.getConversationIdByPeerId(msg.getIdUser());
            messageController.saveMessage(msg.getIdMessage(), conversationId, msg.getIdUser(),
                    msg.getMessage());
            ui.onChatMessage(msg.getIdUser(), msg.getIdMessage(), msg.getMessage());

            Recibido recibido = new Recibido(msg.getIdMessage());
            sendMessageById(msg.getIdUser(), recibido);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRecibido(Recibido recibido) {
        String messageId = recibido.getIdMessage();
        String peerId = recibido.getIp() != null
                ? peerController.getPeerIdByIp(recibido.getIp())
                : null;
        if (peerId != null) {
            messageController.saveReceipt(messageId, peerId);
        }
        ui.onMessageReceived(messageId);
    }

    private void handleHello(SocketClient socketClient, Hello hello) {
        try {
            String peerId = hello.getIdUser();
            if (peerController.getPeerById(peerId) != null) {
                Peer me = peerController.getMyself();
                HelloAccept helloAccept = new HelloAccept(me.getId());
                sendMessageInternal(helloAccept, socketClient);
                connections.put(peerId, socketClient);
                ui.onHelloAccepted(peerId);
            } else {
                HelloReject helloReject = new HelloReject();
                sendMessageInternal(helloReject, socketClient);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleHelloAccept(SocketClient socketClient, HelloAccept helloAccept) {
        String peerId = helloAccept.getIdUser();
        connections.put(peerId, socketClient);
        ui.onHelloAccepted(peerId);
    }

    private void handleHelloReject(HelloReject helloReject) {
        ui.onHelloRejected(helloReject.getIp());
    }

    private void handleOffline(Offline offline) {
        String userName = peerController.getPeerNameByIp(offline.getIp());
        ui.onOfflineReceived(offline.getIdUser(), userName);
    }

    @Override
    public void onClientDisconnected(SocketClient socketClient) {
        Peer peer = PeerDao.getInstance().findByIp(socketClient.getIp());
        if (peer == null) {
            ui.onDisconnect(socketClient.getIp());
            return;
        }
        ui.onUpdatePeerStatus(peer.getId());
    }
}
