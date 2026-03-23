package com.fernandev.chatp2p.view.panel.right;

import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.message.InputPanelState;
import com.fernandev.chatp2p.view.state.theme.rightpanel.ChatInputPanelTheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SendMessageButton extends JButton implements StateListener {
    private StateManager stateManager = StateManager.getInstance();
    private ChatInputPanelTheme theme;

    public SendMessageButton() {
        this.setText("➤");
        applyTheme();
        this.setBackground(theme.getCOLOR_SEND_BUTTON_BG());
        this.setForeground(theme.getCOLOR_SEND_BUTTON_FG());
        this.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        this.setPreferredSize(new Dimension(60, 40));
        this.addActionListener(e -> {
            State state = stateManager.getCurrentState();
            InputPanelState inputPanelState = state.getInputPanelState();
            inputPanelState.setSendMessageButtonClicked(true);
            stateManager.setNewState(state, List.of(InputPanel.class));
        });
    }

    public void applyTheme() {
        this.theme = stateManager.getCurrentState().getTheme().getRightPanelTheme().getChatInputPanelTheme();
    }

    @Override
    public void onChange(State newState) {

    }
}
