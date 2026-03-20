package com.fernandev.chatp2p.view.panel.left;

import com.fernandev.chatp2p.view.ChatUI;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;


@Getter
@Setter
public class LeftPanel extends JPanel implements StateListener {
    private JPanel header;

    private StateManager stateManager = StateManager.getInstance();
    private ConnectButton connectButton;
    private LeftPanelPeerList peerList;

    private JButton offlineModeButton = new JButton("Modo offline");

    private ChatUI mainView;

    public LeftPanel(ChatUI ui) {
        StateManager.getInstance().subscribeToState(this);

        this.mainView = ui;
        this.header = new LeftPanelHeader();
        this.connectButton = new ConnectButton();
        this.peerList = new LeftPanelPeerList();

        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(300, 0));
        this.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        this.add(header, BorderLayout.NORTH);
        this.add(new JScrollPane(peerList), BorderLayout.CENTER);
        this.add(connectButton, BorderLayout.SOUTH);
    }


    @Override
    public void onChange(State newState) {

    }
}
