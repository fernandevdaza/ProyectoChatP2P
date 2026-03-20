package com.fernandev.chatp2p.view.panel.right;

import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.message.InputPanelState;
import com.fernandev.chatp2p.view.state.theme.rightpanel.ChatInputPanelTheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SendOneTimeMessageButton extends JButton implements StateListener {
    private StateManager stateManager = StateManager.getInstance();
    ChatInputPanelTheme theme;

    public SendOneTimeMessageButton(){
        applyTheme();

        this.setText("❶");
        this.setBackground(theme.getCOLOR_ONE_TIME_MESSAGE_BUTTON_BG());
        this.setForeground(theme.getCOLOR_ONE_TIME_MESSAGE_BUTTON_FG());
        this.setFont(new Font("Segoe UI", Font.BOLD, 20));
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        this.setPreferredSize(new Dimension(60, 40));

        this.addActionListener(e -> {
            State state = stateManager.getCurrentState();
            InputPanelState inputPanelState = state.getInputPanelState();
            boolean isOneTimeMessage = inputPanelState.isOneTimeMessageButtonClicked();
            inputPanelState.setOneTimeMessageButtonClicked(!isOneTimeMessage);
            if (!isOneTimeMessage) {
                this.setBackground(theme.getCOLOR_ONE_TIME_MESSAGE_BUTTON_PRESSED_BG());
            } else {
                this.setBackground(theme.getCOLOR_ONE_TIME_MESSAGE_BUTTON_BG());
            }
            stateManager.setNewState(state, List.of());
        });
    }

    public void applyTheme(){
        this.theme = stateManager.getCurrentState().getTheme().getRightPanelTheme().getChatInputPanelTheme();
    }

    @Override
    public void onChange(State newState) {

    }
}
