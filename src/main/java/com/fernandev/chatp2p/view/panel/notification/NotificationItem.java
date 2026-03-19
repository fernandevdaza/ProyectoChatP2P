package com.fernandev.chatp2p.view.panel.notification;

import com.fernandev.chatp2p.view.ChatUI;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.theme.leftpanel.NotificationsTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NotificationItem extends JPanel implements StateListener {
    private StateManager stateManager = StateManager.getInstance();
    private NotificationsTheme theme;

    public NotificationItem(String text){
        stateManager.subscribeToState(this);

        applyTheme();

        this.setLayout(new BorderLayout());
        this.setBackground(theme.getCOLOR_NOTIFICATION_ITEM());
        this.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel label = new JLabel("<html>" + text + "</html>");
        this.add(label, BorderLayout.CENTER);
    }

    private void applyTheme(){
        this.theme = stateManager.getCurrentState().getTheme().getLeftPanelTheme().getNotificationsTheme();
    }

    @Override
    public void onChange(State newState) {

    }
}
