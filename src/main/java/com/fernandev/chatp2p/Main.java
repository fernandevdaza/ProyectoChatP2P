package com.fernandev.chatp2p;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.network.ChatServer;
import com.fernandev.chatp2p.model.network.NetworkUtils;
import com.fernandev.chatp2p.model.repository.CachePeerDao;
import com.fernandev.chatp2p.model.repository.DatabaseConnection;
import com.fernandev.chatp2p.model.repository.IPeerDao;
import com.fernandev.chatp2p.model.repository.PeerDao;
import com.fernandev.chatp2p.view.ChatUI;

import javax.sound.sampled.*;
import javax.swing.*;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.sound.sampled.*;

public class Main {

    public static void main(String[] args) {
        DatabaseConnection.getInstance().initDatabase();
        int port = 1900;
        ConnectionController.getInstance().setPort(1901);
        IPeerDao peerDao = new CachePeerDao(new PeerDao());
        Peer myself = peerDao.findMe();
        if (myself == null) {
            String displayName = JOptionPane.showInputDialog(null,
                    "Bienvenido! Ingresa tu nombre de usuario:",
                    "Registro de usuario",
                    JOptionPane.QUESTION_MESSAGE);

            if (displayName == null || displayName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Debes ingresar un nombre para continuar.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }

            if (displayName.trim().length() > 60) {
                JOptionPane.showMessageDialog(null,
                        "El nombre no puede superar los 60 caracteres.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }

            LocalDateTime now = LocalDateTime.now();
            Peer selfPeer = Peer.builder()
                    .id(UUID.randomUUID().toString())
                    .displayName(displayName.trim())
                    .isSelf(1)
                    .lastIpAddr("127.0.0.1")
                    .lastPort(port)
                    .lastSeenAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            try {
                peerDao.save(selfPeer);
                myself = selfPeer;
                System.out.println("Se creó el peer propio: " + selfPeer.getDisplayName());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error al guardar el usuario: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        }
        myself.setLastIpAddr(PeerController.getInstance().getCurrentWifiLanIp());
        PeerController.getInstance().updatePeer(myself);

        java.awt.EventQueue.invokeLater(() -> {
            Thread.currentThread().setName("UI-Thread");

            final ChatUI chatUI = new ChatUI();
            ConnectionController.getInstance().setUi(chatUI);

            final PeerController peerController = PeerController.getInstance();
            peerController.setView(chatUI);

            chatUI.setPeerController(peerController);

            final MessageController messageController = MessageController.getInstance();
            messageController.setView(chatUI);

            chatUI.setMessageController(messageController);

            ConnectionController.getInstance().setPeerController(peerController);
            ConnectionController.getInstance().setMessageController(messageController);



            new Thread(chatUI::renderPeers, "Initial-Load-Thread").start();


            chatUI.setVisible(true);


        });
        try {
            ChatServer chatServer = new ChatServer(port);
            chatServer.setName("ChatServer");
            chatServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
