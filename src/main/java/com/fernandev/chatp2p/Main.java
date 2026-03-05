/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.fernandev.chatp2p;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.network.ChatServer;
import com.fernandev.chatp2p.view.ChatUI;

public class Main {

    public static void main(String[] args) {
        int port = 1900;
        ConnectionController.getInstance().setPort(1900);

        final ChatUI chatUI = new ChatUI();
        ConnectionController.getInstance().setUI(chatUI);

        final PeerController peerController = new PeerController(chatUI);
        chatUI.setPeerController(peerController);

        final MessageController messageController = MessageController.getInstance();
        chatUI.setMessageController(messageController);

        ConnectionController.getInstance().setPeerController(peerController);
        ConnectionController.getInstance().setMessageController(messageController);

        java.awt.EventQueue.invokeLater(() -> chatUI.setVisible(true));
        try {
            ChatServer chatServer = new ChatServer(port);
            chatServer.setName("ChatServer");
            chatServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        peerController.onLoad();

    }
}
