package com.fernandev.chatp2p.view.state.theme.leftpanel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeftPanelTheme {
    LeftPanelHeaderTheme headerTheme = new LeftPanelHeaderTheme();
    NotificationsTheme notificationsTheme = new NotificationsTheme();
    ConnectButtonTheme connectButtonTheme = new ConnectButtonTheme();
    public LeftPanelTheme(){

    }
}
