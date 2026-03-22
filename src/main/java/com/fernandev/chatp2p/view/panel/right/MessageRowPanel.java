package com.fernandev.chatp2p.view.panel.right;

import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public class MessageRowPanel extends JPanel {
    StateManager stateManager = StateManager.getInstance();

    public MessageRowPanel(Message message, boolean isMe, boolean isReceived){
        State state = stateManager.getCurrentState();
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(4, 8, 4, 8));

        String messageId = message.getId();
        String messageContent = message.getTextContent();
        boolean isOneTimeMessage = message.getIsEphemeral();
        boolean isImage = message.getIsImage();

        MessageBubble messageBubble = new MessageBubble(messageContent, isMe, messageId, isOneTimeMessage, isImage);

        if (isReceived && isMe){
            messageBubble.setReceived(true);
        }

        if (messageId != null) {
            state.getMessageBubbleMap().put(messageId, messageBubble);
        }

        JPanel alignPanel = new JPanel(new FlowLayout(
                isMe ? FlowLayout.RIGHT : FlowLayout.LEFT
        ));

        alignPanel.setOpaque(false);
        alignPanel.add(messageBubble);

        add(alignPanel, BorderLayout.CENTER);

    }
}
