package com.fernandev.chatp2p.view.panel.left;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.messages.Hello;
import com.fernandev.chatp2p.view.ChatUI;
import com.fernandev.chatp2p.view.panel.right.ChatPanel;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.message.ChatPanelState;
import com.fernandev.chatp2p.view.state.peer.LeftPanelPeerListState;
import com.fernandev.chatp2p.view.state.peer.SelectedPeerState;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class LeftPanelPeerList extends JList<Peer> implements StateListener {

    private StateManager stateManager = StateManager.getInstance();
    private Font defaultFont = new Font("Segoe UI", Font.PLAIN, 14);
    private int cellHeight = 50;
    private ImageIcon iconOffline = loadIcon("/view/offline.png");
    private ImageIcon iconOnline = loadIcon("/view/online.png");

    private ImageIcon loadIcon(String path) {
        java.net.URL url = getClass().getResource(path);

        if (url == null) {
            System.err.println("No se encontró el recurso: " + path);
            return new ImageIcon();
        }

        Image image = new ImageIcon(url).getImage()
                .getScaledInstance(20, 20, Image.SCALE_SMOOTH);

        return new ImageIcon(image);
    }

    public LeftPanelPeerList() {
        stateManager.subscribeToState(this);

        Peer[] peers = getPeerList();

        this.setListData(peers);
        this.setFixedCellHeight(cellHeight);
        this.setFont(defaultFont);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        buildCellRenderer();
        buildOnSelectAction();
        buildOnSendHelloAction();

    }

    private Peer[] getPeerList() {
        State state = stateManager.getCurrentState();
        Map<String, Peer> peerMap = state.getPeerMap();
        return peerMap.values().toArray(new Peer[0]);
    }

    private void buildCellRenderer() {
        this.setCellRenderer((list, c, index, isSelected, cellHasFocus) -> {
            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));

            if (isSelected) {
                panel.setBackground(list.getSelectionBackground());
            } else {
                panel.setBackground(list.getBackground());
            }

            JLabel lblIcon = new JLabel(c.getConnected() ? iconOnline : iconOffline);
            panel.add(lblIcon, BorderLayout.WEST);

            JLabel lblText = new JLabel();
            lblText.setText("<html><div style='margin-left:5px;'>" +
                    "<b>" + c.getDisplayName() + "</b><br>" +
                    "<span style='color:gray; font-size:9px'>" + c.getLastIpAddr() + "</span>" +
                    "</div></html>");

            if (isSelected)
                lblText.setForeground(list.getSelectionForeground());

            panel.add(lblText, BorderLayout.CENTER);

            return panel;
        });
    }

    public void buildOnSelectAction() {
        State state = stateManager.getCurrentState();
        this.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Peer selection = this.getSelectedValue();
                if (selection != null) {
                    String id = selection.getId();

                    SelectedPeerState selectedPeerState = state.getSelectedPeer();
                    ChatPanelState chatPanelState = state.getChatPanelState();
                    LeftPanelPeerListState leftPanelPeerListState = state.getLeftPanelPeerListState();
                    selectedPeerState.setPeerId(id);
                    selectedPeerState.setPeer(selection);
                    selectedPeerState.setSelected(true);
                    selectedPeerState.setConnected(selection.getConnected());
                    chatPanelState.setLoading(true);
                    leftPanelPeerListState.setPeerItemClicked(true);

                    stateManager.setNewState(state, List.of(ChatUI.class));
                }
            }
        });
    }

    public void buildOnSendHelloAction() {
        State state = stateManager.getCurrentState();
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    SelectedPeerState selectedPeerState = state.getSelectedPeer();

                    Peer selection = selectedPeerState.getPeer();
                    if (selection != null) {
                        String ip = selection.getLastIpAddr();
                        if (ip != null && !ip.isBlank()) {
                            new Thread(() -> {
                                try {
                                    ConnectionController.getInstance().sendMessage(selection.getId(), new Hello());
                                } catch (Exception ex) {
                                    System.out.println(
                                            "[HELLO] No se pudo enviar hello a " + ip + ": " + ex.getMessage());
                                }
                            }, "hello-doubleclick-thread").start();
                        }
                    }
                }
            }
        });
    }

    public void renderPeers() {
        Map<String, Peer> validPeers = stateManager.getCurrentState().getPeerMap();
        if (!validPeers.isEmpty()) {
            new Thread(() -> {
                this.setListData(validPeers.values().toArray(new Peer[0]));
            }, "load-contacts-thread").start();
        } else {

            List<Peer> peers = PeerController.getInstance().findAllExceptMe();

            for (Peer p : peers) {
                if (p.getIsSelf() == 0) {
                    String ip = p.getLastIpAddr();
                    if (ip == null || ip.isBlank()) {
                        System.out.println("[DB] Peer sin IP válida: " + p.getId());
                        continue;
                    }
                    p.setConnected(false);
                    validPeers.put(p.getId(), p);
                }
            }

            new Thread(() -> {
                SwingUtilities.invokeLater(() -> {

                    State state = stateManager.getCurrentState();

                    state.setPeerMap(validPeers);

                    stateManager.setNewState(state, List.of());

                    this.setListData(validPeers.values().toArray(new Peer[0]));

                });

            }, "load-contacts-thread").start();

        }
    }

    @Override
    public void onChange(State newState) {
        LeftPanelPeerListState leftPanelPeerListState = newState.getLeftPanelPeerListState();
        renderPeers();
        leftPanelPeerListState.setLoading(false);
        leftPanelPeerListState.setPeerListRendered(true);
        stateManager.setNewState(newState, List.of());
    }
}
