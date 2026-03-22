package com.fernandev.chatp2p.view.panel.right;

import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.peer.SelectedPeerState;
import com.fernandev.chatp2p.view.state.theme.rightpanel.RightPanelHeaderTheme;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

@Getter
@Setter
public class RightPanelHeader extends JPanel implements StateListener {

    private StateManager stateManager = StateManager.getInstance();
    JButton buzzButton;
    JButton changeThemeButton;
    JLabel peerNameLabel;
    JPanel rightButtons;
    RightPanelHeaderTheme theme;

    public RightPanelHeader() {
        stateManager.subscribeToState(this);
        boolean isPeerConnected = stateManager.getCurrentState().getSelectedPeer().isConnected();

        applyTheme();

        this.setLayout(new BorderLayout());
        this.setBackground(theme.getCOLOR_HEADER_BG());
        this.setForeground(theme.getCOLOR_HEADER_FG());
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(0, 50));
        this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        this.setName("theme-header");

        buildPeerNameLabel();

        this.buzzButton = new BuzzButton();
        this.changeThemeButton = new ChangeThemeButton();

        buildRightButtons();

        enableRightButtons(isPeerConnected);

        this.add(peerNameLabel, BorderLayout.WEST);
        this.add(rightButtons, BorderLayout.EAST);

    }

    public void buildPeerNameLabel() {

        State state = stateManager.getCurrentState();
        Peer peer = state.getSelectedPeer().getPeer();
        if (peer == null)
            return;
        String labelContactString = peer.getDisplayName();

        this.peerNameLabel = new JLabel("  " + labelContactString);
        this.peerNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        this.peerNameLabel.setVerticalAlignment(JLabel.CENTER);
        this.peerNameLabel.setHorizontalAlignment(JLabel.LEFT);

    }

    public void buildRightButtons() {
        this.rightButtons = new JPanel();
        this.rightButtons.setLayout(new BorderLayout());
        this.rightButtons.setOpaque(false);
        this.rightButtons.setPreferredSize(new Dimension(150, 50));
        this.rightButtons.add(changeThemeButton, BorderLayout.WEST);
        this.rightButtons.add(buzzButton, BorderLayout.EAST);
    }

    public void enableRightButtons(boolean enabled) {
//        SwingUtilities.invokeLater(() -> {
            if (changeThemeButton != null) {
                changeThemeButton.setEnabled(enabled);
            }
            if (buzzButton != null) {
                buzzButton.setEnabled(enabled);
            }

            this.revalidate();
            this.repaint();
//        });
    }

    public void applyTheme() {
        this.theme = stateManager.getCurrentState().getTheme().getRightPanelTheme().getHeaderTheme();
    }

    @Override
    public void onChange(State newState) {
        SelectedPeerState selectedPeerState = newState.getSelectedPeer();

        enableRightButtons(selectedPeerState.isConnected());

        applyTheme();
        this.setBackground(theme.getCOLOR_HEADER_BG());
        this.setForeground(theme.getCOLOR_HEADER_FG());
        this.revalidate();
        this.repaint();
    }
}
