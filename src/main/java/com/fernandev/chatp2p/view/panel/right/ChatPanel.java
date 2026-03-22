package com.fernandev.chatp2p.view.panel.right;

import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.model.entities.db.MessageStatusType;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.message.ChatPanelState;
import com.fernandev.chatp2p.view.state.message.InputPanelState;
import com.fernandev.chatp2p.view.state.message.PinnedMessageState;
import com.fernandev.chatp2p.view.state.peer.SelectedPeerState;
import com.fernandev.chatp2p.view.state.theme.rightpanel.ChatPanelTheme;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatPanel extends JPanel implements StateListener {

    private StateManager stateManager = StateManager.getInstance();
    private ChatPanelTheme theme;
    private JPanel messagesContainer;
    private JScrollPane scrollPane;

    public ChatPanel() {
        stateManager.subscribeToState(this);

        applyTheme();

        setLayout(new BorderLayout());
        messagesContainer = new JPanel();
        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setBackground(theme.getCOLOR_BG_CHAT());

        scrollPane = new JScrollPane();
        scrollPane.setViewportView(messagesContainer);
        scrollPane.setBorder(null);
        this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        this.setVisible(false);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    public void applyTheme() {
        this.theme = stateManager.getCurrentState().getTheme().getRightPanelTheme().getChatPanelTheme();
    }

    public void scrollToBotom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
            this.setVisible(true);
        });
    }

    public void addMessage(String peerId, Message message, String meId, boolean isLoading) {
        State state = stateManager.getCurrentState();
        String selectedPeerId = state.getSelectedPeer().getPeerId();

        if (!isLoading) {
            Map<String, Map<String, Message>> chatHistory = state.getMessageMap();
            chatHistory.putIfAbsent(peerId, new LinkedHashMap<>());
            chatHistory.get(peerId).put(message.getId(), message);
            stateManager.setNewState(state, List.of());
        }

        boolean isMe = Objects.equals(message.getSenderPeerId(), meId);
        boolean isReceived = message.getStatus() == MessageStatusType.RECEIVED;

        if (peerId.equals(selectedPeerId)) {
            MessageRowPanel row = new MessageRowPanel(message, isMe, isReceived);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            messagesContainer.add(row);
            messagesContainer.add(Box.createVerticalStrut(4));

            refreshMessages();
        } else {
            System.out.println("Mensaje recibido de " + peerId + " (en segundo plano)");
        }
    }

    private void refreshMessages() {
        messagesContainer.revalidate();
        messagesContainer.repaint();
    }

    public void clearMessages() {
        messagesContainer.removeAll();
        refreshMessages();
    }

    public void loadSelectedChat(String id, boolean isLoading) {
        String conversationId = MessageController.getInstance().getConversationIdByPeerId(id);
        List<Message> messages = MessageController.getInstance()
                .getConversationMessagesWithConversationId(conversationId);

        State state = stateManager.getCurrentState();

        Map<String, Map<String, Message>> messageMap = state.getMessageMap();

        Map<String, Message> tempMessageMap = new LinkedHashMap<>();

        Message pinnedMessage = null;

        PinnedMessageState pinnedMessageState = state.getPinnedMessage();

        String meId = PeerController.getInstance().getMyself().getId();

        Map<String, Message> peerMessages = messageMap.get(id);

        if (peerMessages != null) {
            peerMessages.clear();
        }

        if (messages != null) {
            for (Message message : messages) {
                boolean isMe = Objects.equals(meId, message.getSenderPeerId());
                if (message.getStatus() != MessageStatusType.RECEIVED && !isMe) {
                    MessageController.getInstance().sendReceipt(message);
                    MessageController.getInstance().setReceived(message);
                }

                tempMessageMap.put(message.getId(), message);

                if (message.getIsFixed()) {
                    pinnedMessage = message;
                }
            }

            if (pinnedMessage != null) {
                pinnedMessageState.setPinnedMessageId(pinnedMessage.getId());
                pinnedMessageState.setPinnedMessageContent(pinnedMessage.getTextContent());
                pinnedMessageState.setShowPinnedMessageBox(pinnedMessage.getIsFixed());
            } else {
                pinnedMessageState.setPinnedMessageId(null);
                pinnedMessageState.setPinnedMessageContent(null);
                pinnedMessageState.setShowPinnedMessageBox(false);
            }

            messageMap.put(id, tempMessageMap);

            stateManager.setNewState(state, List.of(PinnedMessageBox.class));

            for (Message msg : messageMap.get(id).values()) {
                addMessage(id, msg, meId, isLoading);
            }


        }
        scrollToBotom();

    }

    @Override
    public void onChange(State newState) {
        InputPanelState inputPanelState = newState.getInputPanelState();
        ChatPanelState chatPanelState = newState.getChatPanelState();
        SelectedPeerState selectedPeerState = newState.getSelectedPeer();

        applyTheme();
        messagesContainer.setBackground(theme.getCOLOR_BG_CHAT());
        this.revalidate();
        this.repaint();

        if (selectedPeerState.getPeerId() == null) {
            clearMessages();
            this.revalidate();
            this.repaint();
            stateManager.setNewState(newState, List.of());
            return;
        }

        this.setVisible(true);

        String meId = PeerController.getInstance().getMyself().getId();

        if (chatPanelState.isLoading()) {
            clearMessages();
            loadSelectedChat(selectedPeerState.getPeerId(), true);
            chatPanelState.setLoading(false);
        } else {
            Map<String, MessageBubble> messageBubbleMap = newState.getMessageBubbleMap();

            String selectedPeerId = selectedPeerState.getPeerId();
            String lastSentMessageId = inputPanelState.getLastSentMessageId();
            String lastReceivedMessageId = chatPanelState.getLastReceivedMessageId();
            Message message = null;

            if (lastSentMessageId != null) {
                if (!messageBubbleMap.containsKey(lastSentMessageId)) {
                    message = MessageController.getInstance().getMessageById(lastSentMessageId);
                }
            }

            if (lastReceivedMessageId != null) {
                if (!messageBubbleMap.containsKey(lastReceivedMessageId)) {
                    message = MessageController.getInstance().getMessageById(lastReceivedMessageId);
                }
            }

            if (message != null) {
                addMessage(selectedPeerId, message, meId, false);
                scrollToBotom();
            }

            inputPanelState.setSendMessageButtonClicked(false);
            inputPanelState.setSendMessageEnterKeyPressed(false);
            inputPanelState.setSendImageButtonClicked(false);
        }

        stateManager.setNewState(newState, List.of());
    }
}
