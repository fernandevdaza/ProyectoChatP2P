/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.bl.message.*;
import edu.upb.chatupb_v2.mediator.ConnectionMediator;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author rlaredo
 */
public class SocketClient extends Thread {
    private final Socket socket;
    private final String ip;
    private final DataOutputStream dout;
    private final BufferedReader br;
    private List<SocketListener> socketListener = new ArrayList<>();

    public SocketClient(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public SocketClient(String ip, int port) throws IOException {
        this.socket = new Socket(ip, port);
        this.ip = ip;
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

//    public void addListener(SocketListener listener) {
//        this.socketListener.add(listener);
//    }

//    public void removeListener(SocketListener listener){
//        this.socketListener.remove(listener);
//    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = br.readLine()) != null) {
                System.out.println(message);
                String split[] = message.split(Pattern.quote("|"));
                if (split.length == 0) {
                    return;
                }
                System.out.println("Llego");
                switch (split[0]) {
                    case "001": {
                        System.out.println("Es invitacion");
                        Invitacion invitacion = Invitacion.parse(message);
                        invitacion.setIp(this.getIp());
                        notificar(this, invitacion);
                        break;
                    }
                    case "002": {
                        System.out.println("Conexión Aceptada");
                        Aceptar aceptacion = Aceptar.parse(message);
                        aceptacion.setIp(this.getIp());
                        notificar(this, aceptacion);
                        break;
                    }
                    case "003": {
                        System.out.println("Conexión Rechazada");
                        Rechazar rechazar = Rechazar.parse(message);
                        rechazar.setIp(this.getIp());
                        notificar(this, rechazar);
                        break;
                    }
                    case "004": {
                        System.out.println("Hello Recibido!");
                        Hello hello = Hello.parse(message);
                        hello.setIp(this.getIp());
                        notificar(this, hello);
                        break;
                    }
                    case "005": {
                        System.out.println("Hello Aceptado!");
                        HelloAccept helloAccept = HelloAccept.parse(message);
                        helloAccept.setIp(this.getIp());
                        notificar(this, helloAccept);
                        break;
                    }
                    case "006": {
                        System.out.println("Hello Rechazado!");
                        HelloReject helloReject = HelloReject.parse(message);
                        helloReject.setIp(this.getIp());
                        notificar(this, helloReject);
                        break;
                    }
                    case "007": {
                        System.out.println("Nuevo Mensaje!");
                        Mensaje mensaje = Mensaje.parse(message);
                        mensaje.setIp(this.getIp());
                        notificar(this, mensaje);
                        break;
                    }
                    case "0018": {
                        System.out.println("Cliente " + this.getIp() + "está en modo Offline");
                        Offline offline = Offline.parse(message);
                        offline.setIp(this.getIp());
                        notificar(this, offline);
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        onDisconnect(this);
        close();
    }

    public void notificar(SocketClient socketClient, MessageProtocol messageProtocol) {
//        for (SocketListener listener : socketListener) {
//            java.awt.EventQueue.invokeLater(() -> listener.onMessage(socketClient, messageProtocol));
//        }
        try {
            ConnectionMediator.getInstance().receiveMessage(messageProtocol, socketClient);
        } catch (SQLException | ConnectException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDisconnect(SocketClient socketClient){
        ConnectionMediator.getInstance().onDisconnectClient(socketClient);
    }

    public void send(MessageProtocol messageProtocol) throws IOException {
        try {
            dout.write(messageProtocol.generarTrama().getBytes("UTF-8"));
            dout.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getIp(){
        return this.socket.getInetAddress().toString().replace("/", "");
    }

    public String getHostIp(){
        return this.socket.getLocalAddress().toString().replace("/", "");
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
