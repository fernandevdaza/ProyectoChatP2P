/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fernandev.chatp2p.model.network;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.model.entities.command.*;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class SocketClient extends Thread {
    private final Socket socket;
    private final String ip;
    private String peerId;
    private String displayName;
    private final DataOutputStream dout;
    private final BufferedReader br;
    private boolean isRejected = false;
    private SocketListener listener;

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

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public String getPeerId() {
        return this.peerId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = br.readLine()) != null) {
                this.isRejected = false;
                System.out.println(message);
                String split[] = message.split(Pattern.quote("|"));
                if (split.length == 0) {
                    return;
                }
                System.out.println("Llego");
                switch (split[0]) {
                    case "001": {
                        System.out.println("[" + Thread.currentThread().getName() + "] Es invitacion");
                        Invitacion invitacion = Invitacion.parse(message);
                        invitacion.setIp(this.getIp());
                        this.setPeerId(invitacion.getIdUsuario());
                        this.setDisplayName(invitacion.getNombre());
                        notificar(this, invitacion);
                        break;
                    }
                    case "002": {
                        System.out.println("[" + Thread.currentThread().getName() + "] Conexión Aceptada");
                        Aceptar aceptacion = Aceptar.parse(message);
                        aceptacion.setIp(this.getIp());
                        notificar(this, aceptacion);
                        break;
                    }
                    case "003": {
                        System.out.println("[" + Thread.currentThread().getName() + "] Conexión Rechazada");
                        Rechazar rechazar = Rechazar.parse(message);
                        rechazar.setIp(this.getIp());
                        notificar(this, rechazar);
                        this.isRejected = true;
                        break;
                    }
                    case "004": {
                        System.out.println("[" + Thread.currentThread().getName() + "] Hello Recibido!");
                        Hello hello = Hello.parse(message);
                        hello.setIp(this.getIp());
                        this.setPeerId(hello.getIdUser());
                        notificar(this, hello);
                        break;
                    }
                    case "005": {
                        System.out.println("[" + Thread.currentThread().getName() + "] Hello Aceptado!");
                        HelloAccept helloAccept = HelloAccept.parse(message);
                        helloAccept.setIp(this.getIp());
                        notificar(this, helloAccept);
                        break;
                    }
                    case "006": {
                        System.out.println("[" + Thread.currentThread().getName() + "] Hello Rechazado!");
                        HelloReject helloReject = HelloReject.parse(message);
                        helloReject.setIp(this.getIp());
                        notificar(this, helloReject);
                        break;
                    }
                    case "007": {
                        System.out.println("[" + Thread.currentThread().getName() + "] Nuevo Mensaje!");
                        Mensaje mensaje = Mensaje.parse(message);
                        mensaje.setIp(this.getIp());
                        notificar(this, mensaje);
                        break;
                    }
                    case "008": {
                        System.out.println("[" + Thread.currentThread().getName() + "] Mensaje Recibido (ACK)!");
                        Recibido recibido = Recibido.parse(message);
                        recibido.setIp(this.getIp());
                        notificar(this, recibido);
                        break;
                    }
                    case "021": {
                        System.out.println("[" + Thread.currentThread().getName() + "] Nueva Imágen!");
                        MessageImage messageImage = MessageImage.parse(message);
                        messageImage.setIp(this.getIp());
                        notificar(this, messageImage);
                        break;
                    }
                    case "0018": {
                        System.out.println("[" + Thread.currentThread().getName() + "] Cliente " + this.getIp()
                                + "está en modo Offline");
                        Offline offline = Offline.parse(message);
                        offline.setIp(this.getIp());
                        notificar(this, offline);
                        break;
                    }
                }
            }
            if (!isRejected) {
                onDisconnect(this);
            }
            close();
        } catch (IOException e) {
            if (!this.socket.isClosed()) {
//                e.printStackTrace();
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
