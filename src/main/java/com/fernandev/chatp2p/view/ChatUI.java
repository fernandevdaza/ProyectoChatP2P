/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.fernandev.chatp2p.view;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.model.entities.db.MessageStatusType;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.messages.Aceptar;
import com.fernandev.chatp2p.model.entities.protocol.messages.Rechazar;
import com.fernandev.chatp2p.model.entities.protocol.messages.Recibido;
import com.fernandev.chatp2p.view.interfaces.IView;
import com.fernandev.chatp2p.view.panel.left.LeftPanel;
import com.fernandev.chatp2p.view.panel.left.notification.NotificationButton;
import com.fernandev.chatp2p.view.panel.left.notification.NotificationPanel;
import com.fernandev.chatp2p.view.panel.right.*;
import com.fernandev.chatp2p.view.panel.left.LeftPanelPeerList;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.message.ChatPanelState;
import com.fernandev.chatp2p.view.state.message.PinnedMessageState;
import com.fernandev.chatp2p.view.state.notification.NotificationState;
import com.fernandev.chatp2p.view.state.peer.LeftPanelPeerListState;
import com.fernandev.chatp2p.view.state.peer.SelectedPeerState;
import com.fernandev.chatp2p.view.state.theme.Theme;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

@Getter
@Setter
public class ChatUI extends JFrame implements IView, StateListener {

    private StateManager stateManager = StateManager.getInstance();

    private PeerController peerController;
    private MessageController messageController;
    private LeftPanel leftPanel;
    private RightPanel rightPanel;


    public ChatUI() {
        StateManager.getInstance().subscribeToState(this);

        setTitle("Handshake");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        this.leftPanel = new LeftPanel(this);
        add(leftPanel, BorderLayout.WEST);

        this.rightPanel = new RightPanel();
        add(rightPanel, BorderLayout.CENTER);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    new Thread(() -> {
                        ConnectionController.getInstance().shutdown();
                    }).start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                dispose();
                System.exit(0);
            }
        });

    }

    public void onInvitationReceived(String peerId, String nombre) {
        int respuesta = JOptionPane.showConfirmDialog(this, "Llegó la invitación: " + nombre);
        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                SwingUtilities.invokeLater(() -> {
                    ConnectionController.getInstance().sendMessage(peerId, new Aceptar());
                    Peer peer = PeerController.getInstance().getPeerById(peerId);
                    this.addElementToPeerList(peer);
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                ConnectionController.getInstance().sendMessage(peerId, new Rechazar());
            });
        }
    }

    public void onAcceptedReceived(String peerId, String nombre) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                nombre + " aceptó la conexión."));
        Peer peer = PeerController.getInstance().getPeerById(peerId);
        this.addElementToPeerList(peer);
    }

    public void onRejectReceived(String ip) {
        javax.swing.SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, ip + " rechazó la conexión."));
    }

    public void onHelloAcceptReceived(String peerId, boolean isRejected) {
        if (isRejected)
            return;
        this.updatePeerStatus(peerId, true);
    }

    public void onHelloRejectReceived(String ip) {
        javax.swing.SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, ip + " rechazó la conexión."));
    }

    public void onMessageReceived(String peerId, String messageId, boolean isEphemeral, boolean isImage) {
        SwingUtilities.invokeLater(() -> {
            State state = stateManager.getCurrentState();
            ChatPanelState chatPanelState = state.getChatPanelState();

            chatPanelState.setLastReceivedMessageId(messageId);
            chatPanelState.setLastReceivedMessageSenderId(peerId);

            String selectedPeerId = state.getSelectedPeer().getPeerId();

            if (Objects.equals(selectedPeerId, peerId)) {
                stateManager.setNewState(state, List.of(ChatPanel.class));

                if (!isEphemeral) {
                    MessageController.getInstance().updateMessageStatus(messageId, MessageStatusType.RECEIVED);
                    Recibido recibido = new Recibido(messageId);
                    ConnectionController.getInstance().sendMessage(peerId, recibido);
                }

            } else {
                System.out.println("Mensaje recibido de " + peerId + " (en segundo plano)");
            }
        });
    }

    public void onReceiptReceived(String messageId) {
        SwingUtilities.invokeLater(() -> {
            State state = stateManager.getCurrentState();
            Map<String, MessageBubble> messageBubbleMap = state.getMessageBubbleMap();

            MessageBubble bubble = messageBubbleMap.get(messageId);
            if (bubble != null) {
                bubble.setReceived(true);
            }
        });
    }

    public void onDeleteMessageReceived(String messageId) {
        State state = stateManager.getCurrentState();
        ChatPanelState chatPanelState = state.getChatPanelState();
        Map<String, MessageBubble> messageBubbleMap = state.getMessageBubbleMap();

        messageBubbleMap.remove(messageId);
        chatPanelState.setLoading(true);

        stateManager.setNewState(state, List.of(ChatPanel.class));
    }

    public void onBuzz(String peerId) {
        long now = System.currentTimeMillis();
        State state = stateManager.getCurrentState();
        if (now - state.getLastBuzzMillis() < 3000) {
            System.out.println("Buzz ignorado por anti-spam.");
            return;
        }
        state.setLastBuzzMillis(now);

        Peer peer = PeerController.getInstance().getPeerById(peerId);
        this.addNotification("Zumbido recibido de " + peer.getDisplayName());
        this.shakeWindow(this);
        stateManager.setNewState(state, List.of());
    }

    public void onPinMessageReceived(boolean isVisible, String messageId) {
        Message message = MessageController.getInstance().getMessageById(messageId);
        State state = stateManager.getCurrentState();
        PinnedMessageState pinnedMessageState = state.getPinnedMessage();
        if (isVisible) {
            pinnedMessageState.setPinnedMessageId(messageId);
            pinnedMessageState.setShowPinnedMessageBox(true);
            pinnedMessageState.setPinnedMessageContent(message.getTextContent());
        } else {
            pinnedMessageState.setPinnedMessageId(null);
            pinnedMessageState.setShowPinnedMessageBox(false);
            pinnedMessageState.setPinnedMessageContent(null);
        }

        stateManager.setNewState(state, List.of(PinnedMessageBox.class));
    }

    public void onChangeThemeReceived(String peerId, String themeId) {
        String currentPeerId = stateManager.getCurrentState().getSelectedPeer().getPeerId();
        if (currentPeerId != null && currentPeerId.equals(peerId)) {
            ThemeManager.getInstance().applyTheme(themeId);
        }

    }

    public void onOfflineReceived(String peerId) {
        javax.swing.SwingUtilities
                .invokeLater(() -> {
                    Peer peer = PeerController.getInstance().getPeerById(peerId);
                    JOptionPane.showMessageDialog(this, peer.getDisplayName() + " está en modo Offline!");
                });
        this.updatePeerStatus(peerId, false);
    }

    public void addElementToPeerList(Peer peer) {
        SwingUtilities.invokeLater(() -> {
            State state = stateManager.getCurrentState();
            Map<String, Peer> peersList = state.getPeerMap();
            peersList.put(peer.getId(), peer);
            stateManager.setNewState(state, List.of(LeftPanelPeerList.class));
        });
    }

    public void repaintRightPanel() {

        if (this.rightPanel != null) {
            this.rightPanel.unsubscribeAll();
        }

        this.remove(this.rightPanel);

        this.setRightPanel(new RightPanel());

        this.loadSelectedChat();

        this.add(this.rightPanel, BorderLayout.CENTER);

        this.revalidate();
        this.repaint();

    }

    public void repaintLeftPanel() {

        if (this.leftPanel != null) {
            StateManager.getInstance().unsubscribeToState(this.leftPanel);
        }

        this.remove(this.leftPanel);

        this.setLeftPanel(new LeftPanel(this));

        this.add(this.leftPanel, BorderLayout.WEST);

        this.revalidate();
        this.repaint();

    }

    public void renderPeers() {

        State state = stateManager.getCurrentState();
        LeftPanelPeerListState leftPanelPeerListState = state.getLeftPanelPeerListState();
        leftPanelPeerListState.setLoading(true);

        stateManager.setNewState(state, List.of(LeftPanelPeerList.class));

    }

    public void updatePeerStatus(String id, boolean isOnline) {

        SwingUtilities.invokeLater(() -> {
            State state = stateManager.getCurrentState();
            Map<String, Peer> peers = state.getPeerMap();
            SelectedPeerState selectedPeerState = state.getSelectedPeer();

            Peer onUpdateStatusPeer = peers.get(id);

            if (onUpdateStatusPeer != null) {

//                if (Objects.equals(selectedPeerState.getPeerId(), onUpdateStatusPeer.getId())) {
                    onUpdateStatusPeer.setConnected(isOnline);
                    PeerController.getInstance().updatePeer(onUpdateStatusPeer);
//                }

                stateManager.setNewState(state,
                        List.of(LeftPanelPeerList.class, RightPanelHeader.class, InputPanel.class));
            }

        });

    }

    public void loadSelectedChat() {
        State state = stateManager.getCurrentState();
        state.getChatPanelState().setLoading(true);
        stateManager.setNewState(state, List.of(ChatPanel.class));
    }

    public void addNotification(String text) {
        State state = stateManager.getCurrentState();
        NotificationState notificationState = state.getNotificationState();
        List<String> notifications = notificationState.getNotifications();
        int unreadNotificationsCount = notificationState.getUnreadNotificationsCount();

        notifications.addFirst(text);

        notificationState.setUnreadNotificationsCount(unreadNotificationsCount + 1);

        stateManager.setNewState(state, List.of(NotificationButton.class, NotificationPanel.class));
    }

    private void shakeWindow(JFrame frame) {
        Point original = frame.getLocation();
        int distance = 10;
        int times = 12;
        int delay = 25;

        new Thread(() -> {
            try {
                for (int i = 0; i < times; i++) {
                    int x = original.x + (i % 2 == 0 ? distance : -distance);
                    SwingUtilities.invokeLater(() -> frame.setLocation(x, original.y));
                    Thread.sleep(delay);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } finally {
                SwingUtilities.invokeLater(() -> frame.setLocation(original));
            }
        }).start();
    }

    public void onUnexpectedDisconnection(String ip) {
        String message = "Conexión con cliente de ip: " + ip + " cerrada";
        javax.swing.SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message));
    }

    @Override
    public void onChange(State newState) {
        Theme themeState = newState.getTheme();
        SelectedPeerState selectedPeerState = newState.getSelectedPeer();
        LeftPanelPeerListState leftPanelPeerListState = newState.getLeftPanelPeerListState();
        if (selectedPeerState.isSelected() && leftPanelPeerListState.isPeerItemClicked()) {
            this.repaintRightPanel();
            leftPanelPeerListState.setPeerItemClicked(false);
        }else if(themeState.isThemeChanged()){
            this.repaintRightPanel();
        }
        themeState.setThemeChanged(false);
        stateManager.setNewState(newState, List.of());

    }
}