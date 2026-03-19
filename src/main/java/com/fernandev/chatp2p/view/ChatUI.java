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
import com.fernandev.chatp2p.view.panel.LeftPanel;
import com.fernandev.chatp2p.view.panel.RightPanel;
import com.fernandev.chatp2p.view.panel.left.LeftPanelPeerList;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.peer.SelectedPeerState;
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
    private long lastBuzzMillis = 0;
    private LeftPanel leftPanel;
    private RightPanel rightPanel;

    DefaultListModel<Peer> peerDefaultListModel = new DefaultListModel<>();

    private Map<String, List<BubbleData>> chatHistory = new HashMap<>();

    private Map<String, BubbleBubble> bubblesByMessageId = new HashMap<>();

    private final List<String> notifications = new ArrayList<>();

    private int unreadNotificationsCount = 0;

    private Color COLOR_HEADER_BG = new Color(0, 168, 132);
    private Color COLOR_HEADER_FG = Color.WHITE;
    private Color COLOR_BG_CHAT = new Color(236, 229, 221);
    private Color COLOR_INPUT_PANEL_BG = new Color(240, 242, 245);
    private Color COLOR_SEND_BUTTON_BG = new Color(0, 168, 132);
    private Color COLOR_SEND_BUTTON_FG = Color.WHITE;
    private Color COLOR_BUBBLE_ME = new Color(220, 248, 198);
    private Color COLOR_BUBBLE_PEER = new Color(255, 255, 255);
    private Color COLOR_BUBBLE_TEXT_ME = Color.DARK_GRAY;
    private Color COLOR_BUBBLE_TEXT_PEER = Color.DARK_GRAY;
    private Color COLOR_GENERAL_BG = new Color(240, 242, 245);
    private Color COLOR_CHECK = new Color(53, 162, 235);

    public ChatUI() {
        StateManager.getInstance().subscribeToState(this);

        setTitle("Chat P2P");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        this.leftPanel = new LeftPanel(this);
        add(leftPanel, BorderLayout.WEST);

        this.rightPanel = new RightPanel(this);
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

    public void oneMessageReceived(String peerId, String idMessage, String message, boolean isEphemeral) {
        SwingUtilities.invokeLater(() -> {
            this.rightPanel.addMessage(message, false, peerId, idMessage, isEphemeral);
            if (Objects.equals(this.getCurrentChatId(), peerId) && !isEphemeral) {
                MessageController.getInstance().updateMessageStatus(idMessage, MessageStatusType.RECEIVED);
                Recibido recibido = new Recibido(idMessage);
                ConnectionController.getInstance().sendMessage(peerId, recibido);
            }
        });
    }

    public void onImageReceived(String peerId, String idMessage, String base64Image) {
        SwingUtilities.invokeLater(() -> {
            this.rightPanel.addImageMessage(base64Image, false, peerId, idMessage);
            if (Objects.equals(this.getCurrentChatId(), peerId)) {
                Recibido recibido = new Recibido(idMessage);
                ConnectionController.getInstance().sendMessage(peerId, recibido);
            }
        });
    }

    public void onReceiptReceived(String messageId) {
        SwingUtilities.invokeLater(() -> this.rightPanel.markMessageReceived(messageId));
    }

    public void onDeleteMessageReceived() {
        this.repaintRightPanel();
    }

    public void onBuzz(String peerId) {
        long now = System.currentTimeMillis();
        if (now - lastBuzzMillis < 3000) {
            System.out.println("Buzz ignorado por anti-spam.");
            return;
        }
        lastBuzzMillis = now;

        Peer peer = PeerController.getInstance().getPeerById(peerId);
        this.getLeftPanel().addNotification("Zumbido recibido de " + peer.getDisplayName());
        this.getLeftPanel().triggerBuzz();
    }

    public void onPinMessageReceived(boolean isVisible, String messageId) {
        Message message = MessageController.getInstance().getMessageById(messageId);
        this.rightPanel.setPinnedMessageId(messageId);
        this.rightPanel.setShowPinnedMessageBox(isVisible);
        this.rightPanel.setPinnedMessage(message.getTextContent());
    }

    public void onChangeThemeReceived(String themeId) {
        ThemeManager.getInstance().applyTheme(themeId, this);
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
            List<Peer> peersList = state.getPeersList();
            peersList.add(peer);
//            Peer[] peers = peersList.toArray(new Peer[0]);
            stateManager.setNewState(state, List.of(LeftPanelPeerList.class));
        });
    }

    public void repaintRightPanel() {

        if (this.rightPanel != null) {
            StateManager.getInstance().unsubscribeToState(this.rightPanel);
        }

        this.remove(this.rightPanel);

        this.setRightPanel(new RightPanel(this));

        this.getLeftPanel().loadSelectedChat(this.getCurrentChatId());

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


    public String getCurrentChatId() {
        return stateManager.getCurrentState().getSelectedPeer().getPeerId();
    }

    public boolean getContactSelected() {

        return stateManager.getCurrentState().getSelectedPeer().isSelected();
    }

    public boolean getContactSelectedConected() {
        return stateManager.getCurrentState().getSelectedPeer().isConnected();
    }

    public boolean getShowPinnedMessage() {
        return this.rightPanel.getShowPinnedMessageBox();
    }

    public void paintBubbleInRightPanel(String text, boolean isMe, String messageId, boolean received,
            boolean isOneTimeMessage) {
        this.rightPanel.paintBubble(text, isMe, messageId, received, isOneTimeMessage);
    }

    public void setInputEnabledInRightPanel(boolean enabled) {
        this.rightPanel.setInputEnabled(enabled);
    }

    @Override
    public void renderPeers(List<Peer> peers) {

        List<Peer> validPeers = new ArrayList<>();

        for (Peer p : peers) {
            if (p.getIsSelf() == 0) {
                String ip = p.getLastIpAddr();
                if (ip == null || ip.isBlank()) {
                    System.out.println("[DB] Peer sin IP válida: " + p.getId());
                    continue;
                }
                p.setConnected(false);
                validPeers.add(p);
            }
        }

        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {

                State state = stateManager.getCurrentState();

                state.setPeersList(validPeers);

                stateManager.setNewState(state,
                        List.of(LeftPanelPeerList.class));

                if (!this.isVisible()) {
                    this.setVisible(true);
                }

            });

        }, "load-contacts-thread").start();


    }

    public void updatePeerStatus(String id, boolean isOnline) {

        SwingUtilities.invokeLater(() -> {
            State state = stateManager.getCurrentState();
            List<Peer> peers = state.getPeersList();

            for (Peer p : peers) {
                if (Objects.equals(p.getId(), id)) {
                    p.setConnected(isOnline);

                    if (Objects.equals(state.getSelectedPeer().getPeerId(), p.getId())) {
                        this.setSelectedPeerStatus(isOnline, p.getId());
                    }
                    break;
                }
            }
            stateManager.setNewState(state, List.of(LeftPanelPeerList.class));
        });

    }


    public void setSelectedPeerStatus(boolean connected, String id) {
        State state = stateManager.getCurrentState();
        String selectedPeerId = state.getSelectedPeer().getPeerId();
        if (!Objects.equals(selectedPeerId, id))
            return;
        this.getRightPanel().getBuzzButton().setEnabled(connected);
        this.getRightPanel().getThemeButton().setEnabled(connected);
        SwingUtilities.invokeLater(() -> {
            this.setInputEnabledInRightPanel(connected);
        });
    }



    public void onUnexpectedDisconnection(String ip) {
        String message = "Conexión con cliente de ip: " + ip + " cerrada";
        javax.swing.SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message));
    }



//    public static Color getCOLOR_HEADER_BG() {
//        return COLOR_HEADER_BG;
//    }
//
//    public static void setCOLOR_HEADER_BG(Color color) {
//        COLOR_HEADER_BG = color;
//    }
//
//    public static Color getCOLOR_HEADER_FG() {
//        return COLOR_HEADER_FG;
//    }
//
//    public static void setCOLOR_HEADER_FG(Color color) {
//        COLOR_HEADER_FG = color;
//    }
//
//    public static Color getCOLOR_BG_CHAT() {
//        return COLOR_BG_CHAT;
//    }
//
//    public static void setCOLOR_BG_CHAT(Color color) {
//        COLOR_BG_CHAT = color;
//    }
//
//    public static Color getCOLOR_INPUT_PANEL_BG() {
//        return COLOR_INPUT_PANEL_BG;
//    }
//
//    public static void setCOLOR_INPUT_PANEL_BG(Color color) {
//        COLOR_INPUT_PANEL_BG = color;
//    }
//
//    public static Color getCOLOR_SEND_BUTTON_BG() {
//        return COLOR_SEND_BUTTON_BG;
//    }
//
//    public static void setCOLOR_SEND_BUTTON_BG(Color color) {
//        COLOR_SEND_BUTTON_BG = color;
//    }
//
//    public static Color getCOLOR_SEND_BUTTON_FG() {
//        return COLOR_SEND_BUTTON_FG;
//    }
//
//    public static void setCOLOR_SEND_BUTTON_FG(Color color) {
//        COLOR_SEND_BUTTON_FG = color;
//    }
//
//    public static Color getCOLOR_BUBBLE_ME() {
//        return COLOR_BUBBLE_ME;
//    }
//
//    public static void setCOLOR_BUBBLE_ME(Color color) {
//        COLOR_BUBBLE_ME = color;
//    }
//
//    public static Color getCOLOR_BUBBLE_PEER() {
//        return COLOR_BUBBLE_PEER;
//    }
//
//    public static void setCOLOR_BUBBLE_PEER(Color color) {
//        COLOR_BUBBLE_PEER = color;
//    }
//
//    public static Color getCOLOR_BUBBLE_TEXT_ME() {
//        return COLOR_BUBBLE_TEXT_ME;
//    }
//
//    public static void setCOLOR_BUBBLE_TEXT_ME(Color color) {
//        COLOR_BUBBLE_TEXT_ME = color;
//    }
//
//    public static Color getCOLOR_BUBBLE_TEXT_PEER() {
//        return COLOR_BUBBLE_TEXT_PEER;
//    }
//
//    public static void setCOLOR_BUBBLE_TEXT_PEER(Color color) {
//        COLOR_BUBBLE_TEXT_PEER = color;
//    }
//
//    public static Color getCOLOR_GENERAL_BG() {
//        return COLOR_GENERAL_BG;
//    }
//
//    public static void setCOLOR_GENERAL_BG(Color color) {
//        COLOR_GENERAL_BG = color;
//    }
//
//    public static Color getCOLOR_CHECK() {
//        return COLOR_CHECK;
//    }
//
//    public static void setCOLOR_CHECK(Color color) {
//        COLOR_CHECK = color;
//    }

    @Override
    public void onChange(State newState) {

        SelectedPeerState selectedPeerState = newState.getSelectedPeer();
        if (selectedPeerState.isSelected()) {
            this.repaintRightPanel();
            return;
        }

    }
}