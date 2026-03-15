package com.fernandev.chatp2p.model.repository;

import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.model.entities.db.MessageStatusType;
import com.fernandev.chatp2p.model.entities.db.MessageType;
import com.fernandev.chatp2p.model.entities.db.Peer;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class MessageDAO implements IMessageDao{
    private DAOHelper<Message> helper;

    public MessageDAO(){
        this.helper = new DAOHelper<>();
    }

    ResultReader<Message> resultReader = result ->{
        Message message = new Message();
            if (existColumn(result, Message.Column.ID)) {
                message.setId(result.getString(Message.Column.ID));
            }
            if (existColumn(result, Message.Column.CONVERSATION_ID)){
                message.setConversationId(result.getString(Message.Column.CONVERSATION_ID));
            }
            if (existColumn(result, Message.Column.SENDER_PEER_ID)) {
                message.setSenderPeerId(result.getString(Message.Column.SENDER_PEER_ID));
            }
            if (existColumn(result, Message.Column.TYPE)) {
                String stringResult = result.getString(Message.Column.TYPE);
                message.setType(MessageType.valueOf(stringResult));
            }
            if (existColumn(result, Message.Column.TEXT_CONTENT)) {
                message.setTextContent(result.getString(Message.Column.TEXT_CONTENT));
            }
            if (existColumn(result, Message.Column.SENT_AT)) {
                message.setSentAt(LocalDateTime.parse(result.getString(Message.Column.SENT_AT)));
            }
            if (existColumn(result, Message.Column.IS_EPHEMERAL)) {
                message.setEphemeral(result.getBoolean(Message.Column.IS_EPHEMERAL));
            }
            if (existColumn(result, Message.Column.EXPIRES_AT)) {
                message.setExpiresAt(LocalDateTime.parse(result.getString(Message.Column.EXPIRES_AT)));
            }
            if (existColumn(result, Message.Column.STATUS)) {
                String stringResult = result.getString(Message.Column.STATUS);
                message.setStatus(MessageStatusType.valueOf(stringResult));
            }
            if (existColumn(result, Message.Column.RECEIVED_AT)) {
                message.setReceivedAt(LocalDateTime.parse(result.getString(Message.Column.RECEIVED_AT)));
            }
            if (existColumn(result, Message.Column.CREATED_AT)) {
                message.setCreatedAt(LocalDateTime.parse(result.getString(Message.Column.CREATED_AT)));
            }
            if (existColumn(result, Message.Column.UPDATED_AT)) {
                message.setUpdatedAt(LocalDateTime.parse(result.getString(Message.Column.UPDATED_AT)));
            }
            return message;
    };

    public static boolean existColumn(ResultSet result, String columnName){
        try{
            result.findColumn(columnName);
            return true;
        }catch (SQLException e){
            System.out.println("[" + Thread.currentThread().getName() + "] No se encontró la columna: " + e.getMessage());
        }
        return false;
    }

    public List<Message> findAll() throws ConnectException, SQLException {
        String query = "SELECT * FROM messages";
        return helper.executeQuery(query, resultReader);
    }

    public boolean exist(String argument) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM messages WHERE " + argument;
        return helper.executeQueryCount(query, null) == 1;
    }

    public List<Message> findMessagesByConversationId(String conversationId) {
        try{
            String query = "SELECT * FROM messages WHERE conversation_id ='" + conversationId + "'";
            System.out.println(query);
            List<Message> list = helper.executeQuery(query, resultReader);
            if (list.isEmpty()) {
                return null;
            }
            return list;
        }catch (ConnectException | SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public Message findMessagesById(String id) {
        try {
            String query = "SELECT * FROM messages WHERE id ='" + id + "'";
            System.out.println(query);
            List<Message> list = helper.executeQuery(query, resultReader);
            if (list.isEmpty()) {
                return null;
            }
            return list.getFirst();
        }catch (ConnectException | SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public List<Message> findMessagesBySenderPeerId(String senderPeerId) {
        try{
            String query = "SELECT * FROM messages WHERE sender_peer_id ='" + senderPeerId + "'";
            System.out.println(query);
            List<Message> list = helper.executeQuery(query, resultReader);
            if (list.isEmpty()) {
                return null;
            }
            return list;
        } catch (ConnectException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public boolean existById(String id) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM peers WHERE id='" + id + "'";
        return helper.executeQueryCount(query, null) == 1;
    }

    public void updateStatus(Message message, MessageStatusType messageStatusType) {
        try {
            String query = "UPDATE messages SET status=? WHERE id=?";
            QueryParameters params = new QueryParameters() {
                @Override
                public void setParameters(PreparedStatement pst) throws SQLException {
                    pst.setString(1, messageStatusType.toString());
                    pst.setString(2, message.getId());
                }
            };
            helper.update(query, params);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void update(String query) throws Exception {
        helper.update(query, null);
    }

    public void save(Message message) throws Exception {
        String query = "INSERT INTO messages(id, conversation_id, sender_peer_id, type, text_content, sent_at, received_at, is_ephemeral, expires_at, status, created_at, updated_at) values (?,?,?,?,?,?,?,?,?,?,?,?)";
        QueryParameters params = new QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, message.getId());
                pst.setString(2, message.getConversationId());
                pst.setString(3, message.getSenderPeerId());
                pst.setString(4, message.getType().toString());
                pst.setString(5, message.getTextContent());
                pst.setString(6, message.getSentAt().toString());
                pst.setString(7, message.getReceivedAt().toString());
                pst.setString(8, String.valueOf(message.getIsEphemeral()));
                pst.setString(9, message.getExpiresAt().toString());
                pst.setString(10, message.getStatus().toString());
                pst.setString(11, message.getCreatedAt().toString());
                pst.setString(12, message.getUpdatedAt().toString());
            }
        };
        helper.insert(query, params, message);
    }

//    public void update(Peer peer) throws Exception {
//        String query = "UPDATE message SET last_ip_addr=? WHERE id=?";
//        QueryParameters params = new QueryParameters() {
//            @Override
//            public void setParameters(PreparedStatement pst) throws SQLException {
//                pst.setString(1, peer.getLastIpAddr());
//                pst.setString(2, peer.getId());
//            }
//        };
//        helper.update(query, params);
//    }

    public void update(String query, String conditionWhere) throws Exception {
        if (query.trim().endsWith("%s")) {
            query = String.format(query, conditionWhere);
        } else {
            query = String.format("%s %s", query, conditionWhere);
        }
        helper.update(query, null);
    }


}

