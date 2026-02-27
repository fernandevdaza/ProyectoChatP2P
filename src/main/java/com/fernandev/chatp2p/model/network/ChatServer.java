/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fernandev.chatp2p.model.network;

import com.fernandev.chatp2p.controller.ConnectionController;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author rlaredo
 */
public class ChatServer extends Thread {

    private final ServerSocket server;
    public ChatServer(int port) throws IOException {
        this.server = new ServerSocket(port);
    }




    @Override
    public void run() {
        while (true) {
            try {
                SocketClient socketClient = new SocketClient(this.server.accept());
                socketClient.addListener(ConnectionController.getInstance());
                socketClient.start();
                System.out.println();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
