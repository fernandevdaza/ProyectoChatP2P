package com.fernandev.chatp2p.controller;

import com.fernandev.chatp2p.controller.exception.UnreachableException;
import com.fernandev.chatp2p.model.entities.command.*;
import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.model.entities.db.MessageStatusType;
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
import java.util.Objects;

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
            if (ip == null) ip = peerId;
            socketClient = this.connectToPeer(ip);
            connections.put(ip, socketClient);
        }
        if(socketClient.getPeerId() == null && ip != null){
            socketClient.setPeerId(peerId);
        }
        messageProtocol.execute(socketClient);
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


    private void sendMessageInternal(MessageProtocol messageProtocol, SocketClient socketClient) {
        socketClient.send(messageProtocol);
    }

    public void onModeOffline() throws SQLException, ConnectException {
        if (isOffline) {
            this.setOffline(false);
        } else {
            Peer me = peerController.getMyself();
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
        } else if (messageProtocol instanceof MessageImage) {
            handleMessageImage((MessageImage) messageProtocol);
        } else if (messageProtocol instanceof Hello) {
            handleHello(socketClient, (Hello) messageProtocol);
        } else if (messageProtocol instanceof HelloAccept) {
            handleHelloAccept(socketClient, (HelloAccept) messageProtocol);
        } else if (messageProtocol instanceof HelloReject) {
            handleHelloReject((HelloReject) messageProtocol);
        } else if (messageProtocol instanceof Recibido) {
            handleRecibido((Recibido) messageProtocol);
        } else if(messageProtocol instanceof EliminarMensaje) {
            handleEliminarMensaje((EliminarMensaje) messageProtocol);
        } else if (messageProtocol instanceof FijarMensaje) {
            handleFijarMensaje((FijarMensaje) messageProtocol);
        }else if (messageProtocol instanceof Offline) {
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
                this.sendMessage(peerId, new Aceptar());
                ui.onInvitationAccepted(peerId, nombre, ip, socketClient.getPort());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            this.sendMessage(peerId, new Rechazar());
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
        ui.onInvitationRejected(ip);
        // socketClient.close();
    }

    private void handleMensaje(Mensaje msg) {
        try {
            String conversationId = messageController.getConversationIdByPeerId(msg.getIdUser());
            messageController.saveMessage(msg.getIdMessage(), conversationId, msg.getIdUser(),
                    msg.getMessage());
            ui.onChatMessage(msg.getIdUser(), msg.getIdMessage(), msg.getMessage());

            if (Objects.equals(ui.getCurrentChatId(), msg.getIdUser())) {
                MessageController.getInstance().updateMessageStatus(msg.getIdMessage(), MessageStatusType.RECEIVED);
                Recibido recibido = new Recibido(msg.getIdMessage());
                this.sendMessage(msg.getIdUser(), recibido);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleMessageImage(MessageImage msg) {
        try {
            ui.onChatImage(msg.getIdUser(), msg.getIdMessage(), msg.getBase64Image());

            if (Objects.equals(ui.getCurrentChatId(), msg.getIdUser())) {
                Recibido recibido = new Recibido(msg.getIdMessage());
                this.sendMessage(msg.getIdUser(), recibido);
            }

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
            messageController.updateMessageStatus(messageId, MessageStatusType.RECEIVED);
            messageController.saveReceipt(messageId, peerId);
        }
        ui.onMessageReceived(messageId);
    }

    private void handleHello(SocketClient socketClient, Hello hello) {
        try {
            String peerId = hello.getIdUser();
            if (peerController.getPeerById(peerId) != null) {
                this.addConnection(peerId, socketClient);
                ConnectionController.getInstance().sendMessage(peerId, new HelloAccept());
                ui.onHelloAccepted(peerId);
            } else {
                this.addConnection(peerId, socketClient);
                ConnectionController.getInstance().sendMessage(peerId, new HelloReject());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleHelloAccept(SocketClient socketClient, HelloAccept helloAccept) {
        String peerId = helloAccept.getIdUser();
        connections.remove(helloAccept.getIp());
        connections.put(peerId, socketClient);
        ui.onHelloAccepted(peerId);
    }

    private void handleHelloReject(HelloReject helloReject) {
        connections.remove(helloReject.getIp());
        ui.onHelloRejected(helloReject.getIp());
    }

    private void handleEliminarMensaje(EliminarMensaje eliminarMensaje){
        MessageController.getInstance().deleteMessage(eliminarMensaje.getIdMessage());
    }

    private void handleFijarMensaje(FijarMensaje fijarMensaje){
        boolean showPinMessage = ui.getShowPinnedMessage();
        MessageController.getInstance().pinMessage(fijarMensaje.getIdMessage(), !showPinMessage);
    }

    private void handleOffline(Offline offline) {
        String userName = peerController.getPeerNameByIp(offline.getIp());
        ui.onOfflineReceived(offline.getIdUser(), userName);
    }

    @Override
    public void onClientDisconnected(SocketClient socketClient) {
        Peer peer = peerController.getPeerByIp(socketClient.getIp());

        if (peer == null) {
            ui.onDisconnect(socketClient.getIp());
            return;
        }
        ui.onUpdatePeerStatus(peer.getId());
    }
}
