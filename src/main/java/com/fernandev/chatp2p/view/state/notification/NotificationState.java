package com.fernandev.chatp2p.view.state.notification;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NotificationState {
    private List<String> notifications = new ArrayList<>();
    private int unreadNotificationsCount = 0;
    private boolean onClickNotificationPanel = false;

    public NotificationState(){

    }
}
