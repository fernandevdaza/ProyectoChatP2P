/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.bl.server.ChatServer;
import edu.upb.chatupb_v2.mediator.ConnectionMediator;

/**
 * @author rlaredo
 */
public class ChatUPB_V2 {

    public static void main(String[] args) {
               /* Create and display the form */
        int port = 1900;
        ConnectionMediator.getInstance().setPort(1901);
        final ChatUI2 chatUI2 = new ChatUI2();
        java.awt.EventQueue.invokeLater(() -> chatUI2.setVisible(true));
        try {
            ChatServer chatServer = new ChatServer(port);
            chatServer.start();
            chatServer.addListener(chatUI2);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
