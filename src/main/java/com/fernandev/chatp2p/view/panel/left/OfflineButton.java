package com.fernandev.chatp2p.view.panel.left;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.view.ChatUI;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;

import javax.swing.*;
import java.awt.*;

public class OfflineButton extends JButton implements StateListener {
    private StateManager stateManager = StateManager.getInstance();


    public OfflineButton(){
        stateManager.subscribeToState(this);
        this.setBackground(Color.RED);
        this.setForeground(Color.WHITE);
        this.addActionListener(e -> {
            this.setEnabled(false);
            new Thread(() -> {
                try {
                    ConnectionController.getInstance().onModeOffline();
                } catch (Exception error) {
                    System.out.println(error.getMessage());
                } finally {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        this.setText("Modo Online");
                        this.setBackground(Color.GREEN);
                        this.setEnabled(true);
                    });
                }
            }).start();
            boolean isOffline = ConnectionController.getInstance().getOffline();
            if (!isOffline) {
                JOptionPane.showMessageDialog(null, "Modo Offline Activado");
            } else {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    this.setText("Modo Offline");
                    this.setBackground(Color.RED);
                });
                JOptionPane.showMessageDialog(null, "Modo Offline desactivado");
            }
        });
    }

    @Override
    public void onChange(State newState) {

    }
}
