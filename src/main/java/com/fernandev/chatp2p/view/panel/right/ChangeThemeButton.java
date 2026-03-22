package com.fernandev.chatp2p.view.panel.right;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.messages.CambiarTema;
import com.fernandev.chatp2p.view.ThemeManager;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.peer.SelectedPeerState;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

@Getter
@Setter
public class ChangeThemeButton extends JButton implements StateListener {

    private StateManager stateManager = StateManager.getInstance();
    private JPopupMenu themeMenu = new JPopupMenu();
    Map<String, String> themeEntries = ThemeManager.getInstance().getThemeMenuEntries();

    public ChangeThemeButton() {
        boolean isPeerConnected = stateManager.getCurrentState().getSelectedPeer().isConnected();

        this.setText("Tema");
        this.setFont(new Font("Segoe UI", Font.BOLD, 12));
        this.setPreferredSize(new Dimension(100, 50));
        this.setToolTipText("Cambiar tema");
        this.setBackground(new Color(240, 242, 245));
        this.setForeground(Color.DARK_GRAY);
        this.setBorderPainted(true);
        this.setFocusPainted(false);

        if (!isPeerConnected) {
            this.setEnabled(false);
        }

        buildThemeMenu();

        this.addActionListener(e -> themeMenu.show(this, 0, this.getHeight()));

    }

    public void buildThemeMenu() {
        for (Map.Entry<String, String> entry : themeEntries.entrySet()) {
            String themeId = entry.getKey();
            String label = entry.getValue();

            JMenuItem item = new JMenuItem(label);
            item.setFont(new Font("Segoe UI", Font.PLAIN, 14));



            item.addActionListener(e -> {
                State state = stateManager.getCurrentState();
                SelectedPeerState selectedPeerState = state.getSelectedPeer();
                boolean isConnected = selectedPeerState.isConnected();
                String peerId = selectedPeerState.getPeerId();

                if (peerId != null) {
                    Peer memPeer = state.getPeerMap().get(peerId);
                    if (memPeer != null) {
                        memPeer.setThemeId(Integer.parseInt(themeId));
                    }
                    if (selectedPeerState.getPeer() != null) {
                        selectedPeerState.getPeer().setThemeId(Integer.parseInt(themeId));
                    }
                }

                ThemeManager.getInstance().applyTheme(themeId);
                if (isConnected && peerId != null) {
                    PeerController.getInstance().setPeerTheme(peerId, Integer.parseInt(themeId));
                    new Thread(() -> {
                        try {
                            CambiarTema cambiarTema = new CambiarTema();
                            cambiarTema.setIdTema(themeId);
                            ConnectionController.getInstance().sendMessage(selectedPeerState.getPeerId(), cambiarTema);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                }
            });
            themeMenu.add(item);
        }
    }

    @Override
    public void onChange(State newState) {

    }
}
