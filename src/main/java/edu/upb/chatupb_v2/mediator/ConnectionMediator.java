package edu.upb.chatupb_v2.mediator;

import edu.upb.chatupb_v2.bl.message.Message;
import edu.upb.chatupb_v2.bl.server.SocketClient;
import edu.upb.chatupb_v2.bl.server.SocketListener;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ConnectionMediator {
    private final static ConnectionMediator connectionMediator = new ConnectionMediator();
    private final Map<String, SocketClient> connections = new HashMap<>();
    private int port;

    public static ConnectionMediator getInstance(){
        return connectionMediator;
    }

    public void setPort(int port){
        this.port = port;
    }

    public SocketClient connectToPeer(String ip, SocketListener socketListener) throws IOException{
            SocketClient connection = new SocketClient(ip, port);
            connection.addListener(socketListener);
//            this.addConnections(ip, connection);
            connection.start();
            return connection;
    }

    public void addConnections(String id, SocketClient socketClient){
        connections.put(id, socketClient);
    }

    public void sendMessage(String ip, Message message, SocketClient socketClient){
        try{
            socketClient.send(message);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public SocketClient getConnection(String id){
        return connections.get(id);
    }

    public void removeConnection(String id){
        connections.remove(id);
    }

    public void removeAllConnections(){
        connections.clear();
    }

}
