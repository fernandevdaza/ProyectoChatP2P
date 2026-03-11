package com.fernandev.chatp2p.model.repository;

import com.fernandev.chatp2p.model.entities.db.MessageReceipt;

import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class MessageReceiptDAO {
    private DAOHelper<MessageReceipt> helper;
    private static final MessageReceiptDAO instance = new MessageReceiptDAO();

    public static MessageReceiptDAO getInstance() {
        return instance;
    }

    private MessageReceiptDAO() {
        this.helper = new DAOHelper<>();
    }

    ResultReader<MessageReceipt> resultReader = result -> {
        MessageReceipt receipt = new MessageReceipt();
        if (existColumn(result, MessageReceipt.Column.MESSAGE_ID)) {
            receipt.setMessageId(result.getString(MessageReceipt.Column.MESSAGE_ID));
        }
        if (existColumn(result, MessageReceipt.Column.PEER_ID)) {
            receipt.setPeerId(result.getString(MessageReceipt.Column.PEER_ID));
        }
        if (existColumn(result, MessageReceipt.Column.STATUS)) {
            receipt.setStatus(result.getString(MessageReceipt.Column.STATUS));
        }
        if (existColumn(result, MessageReceipt.Column.AT_TIME)) {
            receipt.setAtTime(LocalDateTime.parse(result.getString(MessageReceipt.Column.AT_TIME)));
        }
        return receipt;
    };

    public static boolean existColumn(ResultSet result, String columnName) {
        try {
            result.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            System.out
                    .println("[" + Thread.currentThread().getName() + "] No se encontró la columna: " + e.getMessage());
        }
        return false;
    }

    public void save(MessageReceipt receipt) throws Exception {
        String query = "INSERT INTO message_receipts(message_id, peer_id, status, at_time) VALUES (?,?,?,?)";
        QueryParameters params = new QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, receipt.getMessageId());
                pst.setString(2, receipt.getPeerId());
                pst.setString(3, receipt.getStatus());
                pst.setString(4, receipt.getAtTime().toString());
            }
        };
        helper.insert(query, params, receipt);
    }


    public MessageReceipt findByMessageId(String messageId) {
        try {
            String query = "SELECT * FROM message_receipts WHERE message_id = '" + messageId + "'";
            List<MessageReceipt> list = helper.executeQuery(query, resultReader);
            if (list.isEmpty()) {
                return null;
            }
            return list.getFirst();
        } catch (ConnectException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public boolean existsByMessageId(String messageId) {
        return findByMessageId(messageId) != null;
    }
}
