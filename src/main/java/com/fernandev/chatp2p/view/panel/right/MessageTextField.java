package com.fernandev.chatp2p.view.panel.right;

import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.message.InputPanelState;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MessageTextField extends JTextField implements StateListener {

    private StateManager stateManager = StateManager.getInstance();


    public MessageTextField(){
        this.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 0),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        this.addActionListener(e -> {
            State state = stateManager.getCurrentState();
            InputPanelState inputPanelState = state.getInputPanelState();
            inputPanelState.setSendMessageEnterKeyPressed(true);
            stateManager.setNewState(state, List.of(InputPanel.class));
        });
    }



    @Override
    public void onChange(State newState) {

    }
}
