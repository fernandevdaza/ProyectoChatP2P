package com.fernandev.chatp2p.view.panel.right;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.model.entities.protocol.messages.Zumbido;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;

import javax.swing.*;
import java.awt.*;

public class BuzzButton extends JButton implements StateListener {

    private StateManager stateManager = StateManager.getInstance();

    public BuzzButton(){
        String peerId = stateManager.getCurrentState().getSelectedPeer().getPeerId();
        boolean isPeerConnected = stateManager.getCurrentState().getSelectedPeer().isConnected();

        this.setText("\uD83D\uDD0A");
        this.setFont(new Font("Segoe UI", Font.BOLD, 14));
        this.setPreferredSize(new Dimension(50, 50));
        this.addActionListener(e -> {
            Zumbido zumbido = new Zumbido();
            ConnectionController.getInstance().sendMessage(peerId, zumbido);
        });

        if (!isPeerConnected) {
            this.setEnabled(false);
        }
    }

    @Override
    public void onChange(State newState) {

    }
}
