package com.fernandev.chatp2p.view.panel.left.notification;

import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.notification.NotificationState;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

@Getter
@Setter
public class NotificationButton extends JButton implements StateListener {
    private Font buttonTextFont = new Font(Font.DIALOG, Font.PLAIN, 20);
    private NotificationPanel notificationPanel;
    private NotificationPopUpMenu notificationPopUpMenu;
    private StateManager stateManager = StateManager.getInstance();

    public NotificationButton(){
        stateManager.subscribeToState(this);
        this.setText("🔔");
        this.setFocusPainted(false);
        this.setFont(buttonTextFont);

        this.setPreferredSize(new Dimension(60,50));
        this.notificationPanel = new NotificationPanel();
        this.notificationPopUpMenu = new NotificationPopUpMenu(this.notificationPanel);

        this.addActionListener(e -> {
            State state = stateManager.getCurrentState();

            NotificationState notificationState = state.getNotificationState();
            boolean isClicked = notificationState.isOnClickNotificationPanel();

            notificationState.setOnClickNotificationPanel(!isClicked);
            notificationState.setUnreadNotificationsCount(0);

            stateManager.setNewState(state, List.of(NotificationPanel.class));

            if (!isClicked) this.notificationPopUpMenu.showPopUpMenu(this);

            updateNotificationButtonText();
        });

        updateNotificationButtonText();

    }

    public void updateNotificationButtonText() {
        State state = stateManager.getCurrentState();
        int notificationsCount = state.getNotificationState().getUnreadNotificationsCount();
        if (notificationsCount > 0) {
            this.setText("🔔•");
        } else {
            this.setText("🔔");
        }
    }

    @Override
    public void onChange(State newState) {
        updateNotificationButtonText();

    }
}
