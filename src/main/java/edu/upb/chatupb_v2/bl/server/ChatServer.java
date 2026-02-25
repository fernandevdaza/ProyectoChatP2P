/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.mediator.ConnectionMediator;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author rlaredo
 */
public class ChatServer extends Thread {

    private final ServerSocket server;
    private SocketListener socketListener;
//    private ConnectionMediator connectionMediator;
    public ChatServer(int port) throws IOException {
        this.server = new ServerSocket(port);
//        this.connectionMediator = ConnectionMediator.getInstance();
    }

    public void addListener(SocketListener listener) {
        this.socketListener = listener;
    }



    @Override
    public void run() {
        while (true) {
            try {
                SocketClient socketClient = new SocketClient(this.server.accept());
//                socketClient.addListener(this.socketListener);
//                connectionMediator.addConnections(socketClient.getIp(), socketClient);
                socketClient.start();
                System.out.println();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
