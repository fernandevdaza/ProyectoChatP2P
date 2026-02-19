package edu.upb.chatupb_v2.mediator;

import edu.upb.chatupb_v2.bl.message.MessageProtocol;
import edu.upb.chatupb_v2.bl.server.SocketClient;
import edu.upb.chatupb_v2.bl.server.SocketListener;
import edu.upb.chatupb_v2.repository.*;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.UUID;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ConnectionMediator {
    private final static ConnectionMediator connectionMediator = new ConnectionMediator();
    private final Map<String, SocketClient> connections = new HashMap<>();
    private int port;

    public static ConnectionMediator getInstance() {
        return connectionMediator;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public SocketClient connectToPeer(String ip, SocketListener socketListener) throws IOException {
        SocketClient connection = new SocketClient(ip, port);
        connection.addListener(socketListener);
        connection.start();
        return connection;
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

    public void saveMessage(String ip, String id, String conversation_id, String senderPeerId, String message) throws Exception {
        if (ip == null && id == null && conversation_id == null && senderPeerId == null && message == null) return;
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

    public String getConversationIdByPeerId(String peerId) throws SQLException, ConnectException {
        return DirectParticipantsDAO.getInstance().findConversationByPeerId(peerId).getConversation_id();
    }

    public String getPeerIdByIp(String ip) throws SQLException, ConnectException {
        Peer peer = PeerDao.getInstance().findByIp(ip);
        if (peer == null) return null;
        return peer.getId();
    }

    public String getConversationIdByIp(String ip) throws SQLException, ConnectException {
        Peer peer = PeerDao.getInstance().findByIp(ip);
        return DirectParticipantsDAO.getInstance().findConversationByPeerId(peer.getId()).getConversation_id();
    }

    public Peer getMyself() throws SQLException, ConnectException {
        return PeerDao.getInstance().findMe();
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
