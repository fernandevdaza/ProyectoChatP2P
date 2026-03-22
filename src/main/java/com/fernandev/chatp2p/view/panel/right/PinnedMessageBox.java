package com.fernandev.chatp2p.view.panel.right;

import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.message.PinnedMessageState;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

@Getter
@Setter
public class PinnedMessageBox extends JPanel implements StateListener {
    private StateManager stateManager = StateManager.getInstance();

    private JLabel labelPinnedMessage;
    private JButton unPinButton;

    public PinnedMessageBox() {
        stateManager.subscribeToState(this);

        this.setLayout(new BorderLayout(5, 0));
        this.setBackground(new Color(80, 80, 80));
        this.setPreferredSize(new Dimension(Integer.MAX_VALUE, 35));
        this.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.LIGHT_GRAY));

        buildUnPinButton();
        buildLabelPinnedMessage();

        boolean showPinnedMessageBox = stateManager.getCurrentState().getPinnedMessage().isShowPinnedMessageBox();

        this.add(unPinButton, BorderLayout.WEST);
        this.add(labelPinnedMessage, BorderLayout.CENTER);
        this.setVisible(showPinnedMessageBox);
    }

    public void buildUnPinButton() {
        this.unPinButton = new JButton("✕ Desfijar");
        this.unPinButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        this.unPinButton.setForeground(Color.WHITE);
        this.unPinButton.setBackground(new Color(113, 122, 122));
        this.unPinButton.setBorderPainted(false);
        this.unPinButton.setFocusPainted(false);
        this.unPinButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.unPinButton.setToolTipText("Desfijar mensaje");

        this.unPinButton.addActionListener(e -> {
            PinnedMessageState currentPinnedState = stateManager.getCurrentState().getPinnedMessage();
            String currentPinnedId = currentPinnedState.getPinnedMessageId();
            if (currentPinnedId != null) {
                MessageController.getInstance().pinMessage(currentPinnedId, false, true);
            }
        });
    }

    public void buildLabelPinnedMessage() {
        PinnedMessageState pinnedMessageState = stateManager.getCurrentState().getPinnedMessage();
        String pinnedMessageText = pinnedMessageState.getPinnedMessageContent();

        labelPinnedMessage = new JLabel(pinnedMessageText);
        labelPinnedMessage.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        labelPinnedMessage.setForeground(Color.white);
    }

    public void clearPinnedMessage() {
        this.removeAll();
        this.revalidate();
        this.repaint();
    }

    @Override
    public void onChange(State newState) {
        PinnedMessageState pinnedMessageState = newState.getPinnedMessage();
        boolean show = pinnedMessageState.isShowPinnedMessageBox();

        this.setVisible(show);

        if (show) {
            labelPinnedMessage.setText(pinnedMessageState.getPinnedMessageContent());
        }

        this.revalidate();
        this.repaint();
    }

}
