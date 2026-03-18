/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fernandev.chatp2p.model.network;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.MessageProtocol;
import com.fernandev.chatp2p.model.entities.protocol.command.interfaces.ProtocolCommand;
import com.fernandev.chatp2p.model.entities.protocol.factory.ProtocolCommandFactory;
import com.fernandev.chatp2p.model.entities.protocol.parser.ProtocolParser;

import lombok.*;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Pattern;

@Getter
@Setter
public class SocketClient extends Thread {
    private final Socket socket;
    private final String ip;
    private String peerId;
    private String displayName;
    private final DataOutputStream dout;
    private final BufferedReader br;
    private boolean isRejected = false;
    private SocketListener listener;
    private MessageProtocol lastMessage;

    public SocketClient(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public SocketClient(String ip, int port) throws IOException {
        this.socket = new Socket();
        socket.connect(new InetSocketAddress(ip, port), 3000);
        this.ip = ip;
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public void addListener(SocketListener listener) {
        this.listener = listener;
    }


    @Override
    public void run() {
        try {
            String message;
            while ((message = br.readLine()) != null) {
                this.isRejected = false;
                System.out.println(message);
                String[] split = message.split(Pattern.quote("|"));
                if (split.length == 0) {
                    return;
                }
                MessageProtocol messageProtocol = ProtocolParser.parse(message);
                messageProtocol.setIp(this.getIp());

                this.setLastMessage(messageProtocol);

                System.out.println("[" + Thread.currentThread().getName() + "] " + message);
                notificar(this, messageProtocol);

            }
            if (!isRejected) {
                onDisconnect(this);
            }
            close();
        } catch (IOException e) {
            if (!this.socket.isClosed()) {
                onDisconnect(this);
                close();
            }
        }
    }

    public void notificar(SocketClient socketClient, MessageProtocol messageProtocol) {
        SwingUtilities
                .invokeLater(() -> ConnectionController.getInstance().onMessageReceived(socketClient, messageProtocol));
    }

    public void onDisconnect(SocketClient socketClient) {
        SwingUtilities.invokeLater(() -> ConnectionController.getInstance().onClientDisconnected(socketClient));
    }

    public void send(MessageProtocol messageProtocol) {
        try {
            dout.write(messageProtocol.generarTrama().getBytes("UTF-8"));
            dout.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getIp() {
        return this.socket.getInetAddress().toString().replace("/", "");
    }

    public String getHostIp() {
        return this.socket.getLocalAddress().toString().replace("/", "");
    }

    public int getPort() {
        return this.socket.getPort();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() {
        try {
            this.socket.close();
            this.br.close();
            this.dout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
