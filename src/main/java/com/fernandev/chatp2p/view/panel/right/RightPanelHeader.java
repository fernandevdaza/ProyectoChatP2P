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

    private final StateManager stateManager = StateManager.getInstance();
    private JButton buzzButton;
    private JButton changeThemeButton;
    private JLabel peerNameLabel;
    private JPanel rightButtons;
    private RightPanelHeaderTheme theme;

    public RightPanelHeader() {
        stateManager.subscribeToState(this);

        applyTheme();

        this.setLayout(new BorderLayout());
        this.setBackground(theme.getCOLOR_HEADER_BG());
        this.setForeground(theme.getCOLOR_HEADER_FG());
        this.setPreferredSize(new Dimension(0, 50));
        this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        this.setName("theme-header");

        buildPeerNameLabel();

        this.buzzButton = new BuzzButton();
        this.changeThemeButton = new ChangeThemeButton();

        buildRightButtons();

        SelectedPeerState selectedPeerState = stateManager.getCurrentState().getSelectedPeer();
        boolean hasSelectedPeer = selectedPeerState != null && selectedPeerState.getPeerId() != null;
        boolean enableButtons = hasSelectedPeer && selectedPeerState.isConnected();

        enableRightButtons(enableButtons);

        this.add(peerNameLabel, BorderLayout.WEST);
        this.add(rightButtons, BorderLayout.EAST);
    }

    private void buildPeerNameLabel() {
        this.peerNameLabel = new JLabel("  ");
        this.peerNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        this.peerNameLabel.setVerticalAlignment(JLabel.CENTER);
        this.peerNameLabel.setHorizontalAlignment(JLabel.LEFT);

        updatePeerNameLabel();
    }

    private void updatePeerNameLabel() {
        State state = stateManager.getCurrentState();
        Peer peer = state.getSelectedPeer().getPeer();

        if (peer == null) {
            this.peerNameLabel.setText("  ");
            return;
        }

        this.peerNameLabel.setText("  " + peer.getDisplayName());
    }

    public void buildRightButtons() {
        this.rightButtons = new JPanel(new BorderLayout());
        this.rightButtons.setOpaque(false);
        this.rightButtons.setPreferredSize(new Dimension(150, 50));
        this.rightButtons.add(changeThemeButton, BorderLayout.WEST);
        this.rightButtons.add(buzzButton, BorderLayout.EAST);
    }

    public void enableRightButtons(boolean enabled) {
        if (changeThemeButton != null) {
            changeThemeButton.setEnabled(enabled);
        }
        if (buzzButton != null) {
            buzzButton.setEnabled(enabled);
        }

        this.revalidate();
        this.repaint();
    }

    public void applyTheme() {
        this.theme = stateManager.getCurrentState()
                .getTheme()
                .getRightPanelTheme()
                .getHeaderTheme();
    }

    public void clearHeader() {
        if (peerNameLabel != null) {
            peerNameLabel.setText("  ");
        }

        enableRightButtons(false);
        this.revalidate();
        this.repaint();
    }

    @Override
    public void onChange(State newState) {
        SelectedPeerState selectedPeerState = newState.getSelectedPeer();

        boolean hasSelectedPeer =
                selectedPeerState != null &&
                        selectedPeerState.getPeerId() != null;

        applyTheme();
        this.setBackground(theme.getCOLOR_HEADER_BG());
        this.setForeground(theme.getCOLOR_HEADER_FG());

        if (!hasSelectedPeer) {
            clearHeader();
            return;
        }

        updatePeerNameLabel();
        enableRightButtons(selectedPeerState.isConnected());

        this.revalidate();
        this.repaint();
    }
}