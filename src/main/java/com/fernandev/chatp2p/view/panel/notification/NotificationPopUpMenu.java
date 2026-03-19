package com.fernandev.chatp2p.view.panel.notification;

import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;

import javax.swing.*;
import java.awt.*;

public class NotificationPopUpMenu extends JPopupMenu implements StateListener {
    private StateManager stateManager = StateManager.getInstance();

    public NotificationPopUpMenu(NotificationPanel notificationPanel){
        stateManager.subscribeToState(this);
        this.setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(notificationPanel);
        scrollPane.setPreferredSize(new Dimension(240, 180));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        this.add(scrollPane, BorderLayout.CENTER);
    }

    public void showPopUpMenu(NotificationButton notificationButton){

        this.show(notificationButton, notificationButton.getWidth() - 240, notificationButton.getHeight());
    }


    @Override
    public void onChange(State newState) {

    }
}
