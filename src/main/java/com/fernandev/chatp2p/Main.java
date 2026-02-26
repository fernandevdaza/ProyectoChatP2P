/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.fernandev.chatp2p;

import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.network.ChatServer;
import com.fernandev.chatp2p.controller.ConnectionMediator;
import com.fernandev.chatp2p.view.ChatUI;

public class Main {

    public static void main(String[] args) {
        int port = 1900;
        ConnectionMediator.getInstance().setPort(1901);

        final ChatUI chatUI = new ChatUI();
        ConnectionMediator.getInstance().setUI(chatUI);

        final PeerController peerController = new PeerController(chatUI);
        chatUI.setPeerController(peerController);

        java.awt.EventQueue.invokeLater(() -> chatUI.setVisible(true));
        try {
            ChatServer chatServer = new ChatServer(port);
            chatServer.start();
        }catch (Exception e){
            e.printStackTrace();
        }

        peerController.onLoad();

    }
}
