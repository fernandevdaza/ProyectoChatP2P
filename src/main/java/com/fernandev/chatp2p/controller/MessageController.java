package com.fernandev.chatp2p.controller;

import com.fernandev.chatp2p.model.entities.command.Mensaje;
import com.fernandev.chatp2p.model.entities.command.Recibido;
import com.fernandev.chatp2p.model.entities.db.*;
import com.fernandev.chatp2p.model.repository.ConversationDao;
import com.fernandev.chatp2p.model.repository.DirectParticipantsDAO;
import com.fernandev.chatp2p.model.repository.MessageDAO;
import com.fernandev.chatp2p.model.repository.MessageReceiptDAO;
import com.fernandev.chatp2p.model.repository.PeerDao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MessageController {
    private static final MessageController instance = new MessageController();

    private final ConversationDao conversationDao;
    private final DirectParticipantsDAO directParticipantsDAO;
    private final MessageDAO messageDAO;
    private final MessageReceiptDAO messageReceiptDAO;
    private final PeerDao peerDao;

    private MessageController() {
        this.conversationDao = ConversationDao.getInstance();
        this.directParticipantsDAO = DirectParticipantsDAO.getInstance();
        this.messageDAO = MessageDAO.getInstance();
        this.messageReceiptDAO = MessageReceiptDAO.getInstance();
        this.peerDao = PeerDao.getInstance();
    }

    public static MessageController getInstance() {
        return instance;
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

        conversationDao.save(conversation);
        return conversation.getId();
    }

    public void setPeerToConversation(String conversationId, String peerId) throws Exception {
        DirectParticipants directParticipants = DirectParticipants.builder()
                .conversationId(conversationId)
                .peerId(peerId)
                .build();

        directParticipantsDAO.save(directParticipants);
    }

    public String getConversationIdByPeerId(String peerId) {
        DirectParticipants dp = directParticipantsDAO.findConversationByPeerId(peerId);
        if (dp != null)
            return dp.getConversationId();
        return null;
    }

    public String getConversationIdByIp(String ip) {
        Peer peer = peerDao.findByIp(ip);
        if (peer == null)
            return null;
        DirectParticipants dp = directParticipantsDAO.findConversationByPeerId(peer.getId());
        if (dp == null)
            return null;
        return dp.getConversationId();
    }

    public void saveMessage(String id, String conversationId, String senderPeerId, String message) throws Exception {
        if (id == null && conversationId == null && senderPeerId == null && message == null)
            return;
        Message mensaje = Message.builder()
                .id(id)
                .conversationId(conversationId)
                .senderPeerId(senderPeerId)
                .type(MessageType.values()[2])
                .textContent(message)
                .sentAt(LocalDateTime.now())
                .receivedAt(LocalDateTime.now())
                .isEphemeral(false)
                .expiresAt(LocalDateTime.now())
                .status(MessageStatusType.SENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        messageDAO.save(mensaje);
    }

    public List<Message> getConversationMessagesWithConversationId(String conversationId) {
        return messageDAO.findMessagesByConversationId(conversationId);
    }

    public void saveReceipt(String messageId, String peerId) {
        try {
            MessageReceipt receipt = MessageReceipt.builder()
                    .messageId(messageId)
                    .peerId(peerId)
                    .status("DELIVERED")
                    .atTime(LocalDateTime.now())
                    .build();
            messageReceiptDAO.save(receipt);
        } catch (Exception e) {
            System.out.println("[RECEIPT] Error al guardar receipt: " + e.getMessage());
        }
    }

    public boolean hasReceipt(String messageId) {
        return messageReceiptDAO.existsByMessageId(messageId);
    }

    public void sendReceipt(Message msg){
        Recibido recibido = new Recibido(msg.getId());
        ConnectionController.getInstance().sendMessageById(msg.getSenderPeerId(), recibido);
    }

    public void setReceived(Message msg){
        MessageDAO.getInstance().updateStatus(msg, MessageStatusType.RECEIVED);
    }
}
