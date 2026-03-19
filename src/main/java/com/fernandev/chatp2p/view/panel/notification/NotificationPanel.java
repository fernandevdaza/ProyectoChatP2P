package com.fernandev.chatp2p.view.panel.notification;

import com.fernandev.chatp2p.view.ChatUI;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.theme.leftpanel.NotificationsTheme;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

@Getter
@Setter
public class NotificationPanel extends JPanel implements StateListener {

    private StateManager stateManager = StateManager.getInstance();
    private NotificationsTheme theme;


    public NotificationPanel(){
        stateManager.subscribeToState(this);

        applyTheme();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(new EmptyBorder(8, 8, 8, 8));
        this.setBackground(theme.getCOLOR_NOTIFICATION_ITEM());
    }

    private void refreshPopupNotification() {
        this.removeAll();
        State state = stateManager.getCurrentState();
        List<String> notifications = state.getNotificationState().getNotifications();

        if (notifications.isEmpty()) {
            JLabel emptyLabel = new JLabel("No hay notificaciones");
            emptyLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
            this.add(emptyLabel);
        } else {
            for (String notification : notifications) {
                JPanel item = new NotificationItem(notification);
                this.add(item);
                this.add(Box.createVerticalStrut(6));
            }
        }

        this.revalidate();
        this.repaint();
    }

    private void applyTheme(){
        this.theme = stateManager.getCurrentState().getTheme().getLeftPanelTheme().getNotificationsTheme();
    }

    @Override
    public void onChange(State newState) {
        if (newState.getNotificationState().isOnClickNotificationPanel()){
            refreshPopupNotification();
        }

    }
}
