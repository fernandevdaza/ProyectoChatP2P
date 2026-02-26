package com.fernandev.chatp2p.controller;

import com.fernandev.chatp2p.model.entities.db.*;
import com.fernandev.chatp2p.view.ChatUI;
import com.fernandev.chatp2p.model.entities.command.Hello;
import com.fernandev.chatp2p.model.entities.command.MessageProtocol;
import com.fernandev.chatp2p.model.entities.command.Offline;
import com.fernandev.chatp2p.model.entities.db.*;
import com.fernandev.chatp2p.model.network.SocketClient;
import com.fernandev.chatp2p.model.repository.ConversationDao;
import com.fernandev.chatp2p.model.repository.DirectParticipantsDAO;
import com.fernandev.chatp2p.model.repository.MessageDAO;
import com.fernandev.chatp2p.model.repository.PeerDao;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ConnectionMediator {
    private final static ConnectionMediator connectionMediator = new ConnectionMediator();
    private final Map<String, SocketClient> connections = new HashMap<>();
    private ChatUI ui;
    private int port;
    private boolean isOffline = false;

    public static ConnectionMediator getInstance() {
        return connectionMediator;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUI(ChatUI ui){
        this.ui = ui;
    }

//    public SocketClient connectToPeer(String ip, SocketListener socketListener) throws IOException {
    public SocketClient connectToPeer(String ip) {
        try{
            SocketClient connection = new SocketClient(ip, port);
            connection.start();
            return connection;
        }catch (IOException e){
            return null;
        }
    }

    public void addConnections(String id, SocketClient socketClient) {
        connections.put(id, socketClient);
    }

    public void sendMessage(MessageProtocol messageProtocol, SocketClient socketClient) {
        try {
            socketClient.send(messageProtocol);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public String getPeerDisplayNameById(String id){
        Peer peer = PeerDao.getInstance().findById(id);
        if (peer != null) return peer.getDisplayName();
        return null;
    }

    public void receiveMessage(MessageProtocol messageProtocol, SocketClient socketClient) throws SQLException, ConnectException {
        if(this.isOffline){
            Offline offline = new Offline(this.getMyself().getId());
            this.sendMessage(offline, socketClient);
        }else{
            ui.onMessage(socketClient, messageProtocol);
        }
    }

    public void sendHelloToPeer(String ip){
        try{
            Peer me = this.getMyself();
            String peerId = this.getPeerIdByIp(ip);
            SocketClient socketClient = this.connectToPeer(ip);
            if (me == null || peerId == null || socketClient == null ){
                System.out.println("Hubo un problema al hacer HelloRequest");
                return;
            }
            MessageProtocol hello = new Hello(me.getId());
            this.sendMessage(hello, socketClient);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onModeOffline() throws SQLException, ConnectException {
        if (isOffline){
            this.setOffline(false);
        }else{
            Offline offline = new Offline(this.getMyself().getId());
            connections.forEach((id, socketClient) -> {
                this.sendMessage(offline, socketClient);
            });
            this.setOffline(true);
        }
    }

    public boolean getOffline(){
        return this.isOffline;
    }

    public void onDisconnectClient(SocketClient socketClient){
        String ip = socketClient.getHostIp();
        this.removeConnection(ip, true);

        String id = this.getPeerIdByIp(ip);
        ui.onDisconnect(id);
    }


    public void setOffline(boolean isOffline){
        this.isOffline = isOffline;
    }

    public void savePeer(String ip, String id, String displayName) throws Exception {
        if (ip == null && id == null && displayName == null) return;
        Peer peer = Peer.builder()
                .id(id)
                .displayName(displayName)
                .isSelf(0)
                .lastIpAddr(ip)
                .lastPort(port)
                .lastSeenAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        PeerDao.getInstance().save(peer);
    }

    public String createConversation() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Conversation conversation = Conversation.builder()
                .id(uuid)
                .type(ConversationType.values()[1])
                .title(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ConversationDao.getInstance().save(conversation);

        return conversation.getId();
    }

    public void setPeerToConversation(String conversation_id, String peer_id) throws Exception {
        DirectParticipants directParticipants = DirectParticipants.builder()
                .conversation_id(conversation_id)
                .peer_id(peer_id)
                .build();

        DirectParticipantsDAO.getInstance().save(directParticipants);
    }

    public String getConversationIdWithPeerId(String peerId){
        DirectParticipants directParticipants = DirectParticipantsDAO.getInstance().findConversationByPeerId(peerId);
        if (directParticipants != null) return directParticipants.getConversation_id();
        return null;
    }

    public void saveMessage(String id, String conversation_id, String senderPeerId, String message) throws Exception {
        if (id == null && conversation_id == null && senderPeerId == null && message == null) return;
        Message mensaje = Message.builder()
                .id(id)
                .conversation_id(conversation_id)
                .sender_peer_id(senderPeerId)
                .type(MessageType.values()[2])
                .text_content(message)
                .sent_at(LocalDateTime.now())
                .received_at(LocalDateTime.now())
                .is_ephemeral(false)
                .expires_at(LocalDateTime.now())
                .status(MessageStatusType.SENT)
                .created_at(LocalDateTime.now())
                .updated_at(LocalDateTime.now())
                .build();
        MessageDAO.getInstance().save(mensaje);
    }

    public String getConversationIdByPeerId(String peerId){
        return DirectParticipantsDAO.getInstance().findConversationByPeerId(peerId).getConversation_id();
    }

    public List<Message> getConversationMessagesWithConversationId(String conversationId){
        return MessageDAO.getInstance().findMessagesByConversationId(conversationId);
    }


    public String getPeerIdByIp(String ip) {
            Peer peer = PeerDao.getInstance().findByIp(ip);
            if (peer == null) return null;
            return peer.getId();
    }

    public String getPeerIpById(String id) {
        Peer peer = PeerDao.getInstance().findById(id);
        if (peer == null) return null;
        return peer.getLastIpAddr();
    }

    public String getPeerNameByIp(String ip) throws SQLException, ConnectException {
        Peer peer = PeerDao.getInstance().findByIp(ip);
        if (peer == null) return null;
        return peer.getDisplayName();
    }

    public String getConversationIdByIp(String ip) throws SQLException, ConnectException {
        Peer peer = PeerDao.getInstance().findByIp(ip);
        return DirectParticipantsDAO.getInstance().findConversationByPeerId(peer.getId()).getConversation_id();
    }

    public Peer getMyself() {
            return PeerDao.getInstance().findMe();
    }

    public SocketClient getConnection(String id){
        return connections.get(id);
    }

    public void closeConnectionWithPeer(String id){
       SocketClient socketClient = connections.get(id);
       socketClient.interrupt();
    }

    public void removeConnection(String id, boolean disconnected){

        if (!disconnected){
            closeConnectionWithPeer(id);
        }
        connections.remove(id);
    }


    public void removeAllConnections(){
        for(String id: connections.keySet()){
            this.removeConnection(id, false);
        }
        connections.clear();
    }

    public void shutdown(){
        this.removeAllConnections();
    }

}
