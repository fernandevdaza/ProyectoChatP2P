package com.fernandev.chatp2p.model.repository;

import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.model.entities.db.MessageStatusType;
import com.fernandev.chatp2p.model.entities.db.Peer;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.List;

public interface IMessageDao {
    List<Message> findAll() throws ConnectException, SQLException;
    boolean exist(String argument) throws ConnectException, SQLException;
    List<Message> findMessagesByConversationId(String conversationId);
    Message findMessageById(String id);
    List<Message> findMessagesBySenderPeerId(String senderPeerId);
    boolean existById(String id) throws ConnectException, SQLException;
    void updateStatus(Message message, MessageStatusType messageStatusType);
    void update(String query) throws Exception;
    void save(Message message) throws Exception;
    void update(String query, String conditionWhere) throws Exception;
}
