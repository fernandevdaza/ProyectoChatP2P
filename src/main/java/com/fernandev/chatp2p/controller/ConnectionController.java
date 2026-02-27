package com.fernandev.chatp2p.controller;

import com.fernandev.chatp2p.model.entities.command.Hello;
import com.fernandev.chatp2p.model.entities.command.MessageProtocol;
import com.fernandev.chatp2p.model.entities.command.Offline;
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

    public SocketClient connectToPeer(String ip) {
        try {
            SocketClient connection = new SocketClient(ip, port);
            connection.addListener(this);
            connection.start();
            return connection;
        } catch (IOException e) {
            return null;
        }
    }

    public void addConnection(String id, SocketClient socketClient) {
        connections.put(id, socketClient);
    }

    public SocketClient getConnection(String id) {
        return connections.get(id);
    }

    public void sendMessage(MessageProtocol messageProtocol, SocketClient socketClient) {
        try {
            socketClient.send(messageProtocol);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    public void sendHelloToPeer(String ip) {
        try {
            Peer me = PeerDao.getInstance().findMe();
            String peerId = PeerDao.getInstance().findByIp(ip) != null ? PeerDao.getInstance().findByIp(ip).getId()
                    : null;
            SocketClient socketClient = this.connectToPeer(ip);
            if (me == null || peerId == null || socketClient == null) {
                System.out.println("Hubo un problema al hacer HelloRequest");
                return;
            }
            MessageProtocol hello = new Hello(me.getId());
            this.sendMessage(hello, socketClient);
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
                this.sendMessage(offline, socketClient);
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
        socketClient.interrupt();
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
            this.sendMessage(offline, socketClient);
        } else {
            ui.onMessage(socketClient, messageProtocol);
        }

    }

    @Override
    public void onClientDisconnected(SocketClient socketClient) {

    }
}
