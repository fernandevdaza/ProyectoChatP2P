package com.fernandev.chatp2p.view.panel.left;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.exception.UnreachableException;
import com.fernandev.chatp2p.model.entities.protocol.messages.Invitacion;
import com.fernandev.chatp2p.view.ChatUI;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.theme.leftpanel.ConnectButtonTheme;
import com.fernandev.chatp2p.view.state.theme.leftpanel.NotificationsTheme;

import javax.swing.*;
import java.awt.*;

public class ConnectButton extends JButton implements StateListener {

    private StateManager stateManager = StateManager.getInstance();
    private ConnectButtonTheme theme;


    public ConnectButton(){
        stateManager.subscribeToState(this);

        applyTheme();

        this.setText("Nueva conexión (+)");
        this.addActionListener(e -> {
            this.setEnabled(false);
            String ip = JOptionPane.showInputDialog(this, "Ingresa IP:");
            if (ip != null && !ip.isEmpty()) {
                new Thread(() -> {
                    try {
                        ConnectionController.getInstance().sendMessage(ip, new Invitacion());
                    } catch (UnreachableException ue) {
                        javax.swing.SwingUtilities
                                .invokeLater(() -> JOptionPane.showMessageDialog(this, ue.getMessage()));
                    } catch (Exception error) {
                        System.out.println(error.getMessage());
                    } finally {
                        javax.swing.SwingUtilities.invokeLater(() -> this.setEnabled(true));
                    }
                }).start();
            } else {
                this.setEnabled(true);
            }
        });
    }

    private void applyTheme(){
        this.theme = stateManager.getCurrentState().getTheme().getLeftPanelTheme().getConnectButtonTheme();
        this.setBackground(theme.getCOLOR_CONNECT_BUTTON());
        this.setForeground(Color.WHITE);
    }


    @Override
    public void onChange(State newState) {
        this.applyTheme();
    }
}
