package com.fernandev.chatp2p.view.panel.left;

import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.view.panel.left.notification.NotificationButton;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.theme.leftpanel.LeftPanelHeaderTheme;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

@Getter
@Setter
public class LeftPanelHeader extends JPanel implements StateListener {

    private StateManager stateManager = StateManager.getInstance();

    LeftPanelHeaderTheme theme;
    JLabel userNameLabel;
    JLabel userIpLabel;
    JPanel userLabelPanels;
    NotificationButton notificationButton;

    public LeftPanelHeader(){
        stateManager.subscribeToState(this);

        applyTheme();

        this.setLayout(new BorderLayout());
        this.setBackground(theme.getCOLOR_GENERAL_BG());
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(300, 40));

        this.notificationButton = new NotificationButton();

        buildUserLabelPanel();

        this.add(this.userLabelPanels, BorderLayout.WEST);
        this.add(this.notificationButton, BorderLayout.EAST);

    }

    private void buildUserNameLabel(String name){
        this.userNameLabel = new JLabel("Bienvenido, " + name + "!");
        this.userNameLabel.setLayout(new BorderLayout());
        this.userNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        this.userNameLabel.setPreferredSize(new Dimension(250, 25));
    }

    private void buildUserIpLabel(String ip){
        this.userIpLabel = new JLabel(ip);
        this.userIpLabel.setLayout(new BorderLayout());
        this.userIpLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        this.userIpLabel.setPreferredSize(new Dimension(250, 15));

    }

    private void buildUserLabelPanel(){
        this.userLabelPanels = new JPanel();
        Peer me = PeerController.getInstance().getMyself();

        buildUserNameLabel(me.getDisplayName());
        buildUserIpLabel(me.getLastIpAddr());

        this.userLabelPanels.setLayout(new BorderLayout());
        this.userLabelPanels.setPreferredSize(new Dimension(250, 40));
        this.userLabelPanels.add(this.userNameLabel, BorderLayout.NORTH);
        this.userLabelPanels.add(this.userIpLabel, BorderLayout.SOUTH);
    }

    private void applyTheme(){
        this.theme = stateManager.getCurrentState().getTheme().getLeftPanelTheme().getHeaderTheme();
    }

    @Override
    public void onChange(State newState) {

    }
}
