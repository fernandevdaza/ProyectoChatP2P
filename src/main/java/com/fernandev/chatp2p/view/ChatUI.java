/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.fernandev.chatp2p.view;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.messages.Aceptar;
import com.fernandev.chatp2p.model.entities.protocol.messages.Rechazar;
import com.fernandev.chatp2p.view.interfaces.IView;
import com.fernandev.chatp2p.view.panel.LeftPanel;
import com.fernandev.chatp2p.view.panel.RightPanel;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public class ChatUI extends javax.swing.JFrame implements IView {

    private PeerController peerController;
    private MessageController messageController;

    private boolean isContactSelected = false;
    private boolean isContactSelectedConnected = false;

    private String currentChatId = null;

    private LeftPanel leftPanel;
    private RightPanel rightPanel;

    DefaultListModel<Peer> peerDefaultListModel = new DefaultListModel<>();

    private Map<String, List<BubbleData>> chatHistory = new HashMap<>();

    private Map<String, BubbleBubble> bubblesByMessageId = new HashMap<>();

    private final List<String> notifications = new ArrayList<>();

    private int unreadNotificationsCount = 0;

    private static Color COLOR_HEADER_BG = new Color(0, 168, 132);
    private static Color COLOR_HEADER_FG = Color.WHITE;
    private static Color COLOR_BG_CHAT = new Color(236, 229, 221);
    private static Color COLOR_INPUT_PANEL_BG = new Color(240, 242, 245);
    private static Color COLOR_SEND_BUTTON_BG = new Color(0, 168, 132);
    private static Color COLOR_SEND_BUTTON_FG = Color.WHITE;
    private static Color COLOR_BUBBLE_ME = new Color(220, 248, 198);
    private static Color COLOR_BUBBLE_PEER = new Color(255, 255, 255);
    private static Color COLOR_BUBBLE_TEXT_ME = Color.DARK_GRAY;
    private static Color COLOR_BUBBLE_TEXT_PEER = Color.DARK_GRAY;
    private static Color COLOR_GENERAL_BG = new Color(240, 242, 245);
    private static Color COLOR_CHECK = new Color(53, 162, 235);

    public ChatUI() {
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

    public void repaintRightPanel() {

        this.remove(this.rightPanel);

        this.setRightPanel(new RightPanel(this));

        this.getLeftPanel().loadSelectedChat(this.currentChatId);

        this.add(this.rightPanel, BorderLayout.CENTER);

        this.revalidate();
        this.repaint();

    }

    @Override
    public RightPanel getRightPanel() {
        return this.rightPanel;
    }

    @Override
    public void setRightPanel(RightPanel rightPanel) {
        this.rightPanel = rightPanel;

    }

    @Override
    public LeftPanel getLeftPanel() {
        return this.leftPanel;
    }

    @Override
    public void setLeftPanel(LeftPanel leftPanel) {

    }

    public String getCurrentChatId() {
        return this.currentChatId;
    }

    public void setCurrentChatId(String id) {
        this.currentChatId = id;
    }

    public boolean getContactSelected() {
        return this.isContactSelected;
    }

    public void setContactSelected(boolean selected) {
        this.isContactSelected = selected;
    }

    public boolean getContactSelectedConected() {
        return this.isContactSelectedConnected;
    }

    public void setContactSelectedConnected(boolean connected) {
        this.isContactSelectedConnected = connected;
    }

    public PeerController getPeerController() {
        return this.peerController;
    }

    public void setPeerController(PeerController peerController) {
        this.peerController = peerController;
    }

    public DefaultListModel<Peer> getPeerDefaultListModel() {
        return peerDefaultListModel;
    }

    public Map<String, List<BubbleData>> getChatHistory() {
        return chatHistory;
    }

    public Map<String, BubbleBubble> getBubblesByMessageId() {
        return bubblesByMessageId;
    }

    public List<String> getNotifications() {
        return notifications;
    }

    public int getUnreadNotificationsCount() {
        return unreadNotificationsCount;
    }

    public void setUnreadNotificationsCount(int unreadNotificationsCount) {
        this.unreadNotificationsCount = unreadNotificationsCount;
    }

    public MessageController getMessageController() {
        return this.messageController;
    }

    public void setMessageController(MessageController messageController) {
        this.messageController = messageController;
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

    public void setPinMessage(boolean isVisible, String message, String messageId) {
        this.rightPanel.setPinnedMessageId(messageId);
        this.rightPanel.setShowPinnedMessageBox(isVisible);
        this.rightPanel.setPinnedMessage(message);
    }

    @Override
    public void onLoad(List<Peer> peers) {

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
                peerDefaultListModel.clear();

                peerDefaultListModel.addAll(validPeers);

                // leftPanel.setListModel(listModel);
            });

            for (Peer p : validPeers) {
                String ip = p.getLastIpAddr();
                if (ip == null || ip.isBlank())
                    continue;
                try {
                    // ConnectionController.getInstance().sendHelloToPeer(ip);
                } catch (Exception ex) {
                    System.out.println("[HELLO] No se pudo enviar hello a " + ip + ": " + ex.getMessage());
                }
            }

        }, "load-contacts-thread").start();

    }

    public void addNotification(String text) {
        this.getLeftPanel().addNotification(text);
        this.getLeftPanel().triggerBuzz();
    }

    public void onUpdatePeerStatus(String id) {
        this.leftPanel.updatePeerStatus(id, false);
    }

    public void onDisconnect(String ip) {
        String message = "Conexión con cliente de ip: " + ip + " cerrada";
        javax.swing.SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message));
    }

    public void onInvitationReceived(String peerId, String nombre) {
        int respuesta = JOptionPane.showConfirmDialog(this, "Llegó la invitación: " + nombre);
        if(respuesta == JOptionPane.YES_OPTION){
            try {
                SwingUtilities.invokeLater(() -> {
                    ConnectionController.getInstance().sendMessage(peerId, new Aceptar());
                    Peer peer = PeerController.getInstance().getPeerById(peerId);
                    this.addElementToPeerList(peer);
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else {
            SwingUtilities.invokeLater(() -> {
                ConnectionController.getInstance().sendMessage(peerId, new Rechazar());
            });
        }
    }

    public void onInvitationAccepted(String peerId, String nombre) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                nombre + " aceptó la conexión."));
        Peer peer = PeerController.getInstance().getPeerById(peerId);
        this.addElementToPeerList(peer);
    }

    public void addElementToPeerList(Peer peer){
        SwingUtilities.invokeLater(() -> peerDefaultListModel.addElement(peer));
    }

    public void onInvitationRejected(String ip) {
        javax.swing.SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, ip + " rechazó la conexión."));
    }

    public void onChatMessage(String peerId, String idMessage, String message, boolean isEphemeral) {
        SwingUtilities.invokeLater(() -> this.rightPanel.addMessage(message, false, peerId, idMessage, isEphemeral));
    }

    public void onChatImage(String peerId, String idMessage, String base64Image) {
        SwingUtilities.invokeLater(() -> this.rightPanel.addImageMessage(base64Image, false, peerId, idMessage));
    }

    public void onHelloAccepted(String peerId) {
        this.leftPanel.updatePeerStatus(peerId, true);
    }

    public void onHelloRejected(String ip) {
        javax.swing.SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, ip + " rechazó la conexión."));
    }

    public void onOfflineReceived(String peerId, String userName) {
        javax.swing.SwingUtilities
                .invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, userName + " está en modo Offline!");
                });
        this.leftPanel.updatePeerStatus(peerId, false);
    }

    public void onMessageReceived(String messageId) {
        SwingUtilities.invokeLater(() -> this.rightPanel.markMessageReceived(messageId));
    }

    public void onThemeChanged(String themeId) {
        ThemeManager.getInstance().applyTheme(themeId, this);
    }

    public static Color getCOLOR_HEADER_BG() {
        return COLOR_HEADER_BG;
    }

    public static void setCOLOR_HEADER_BG(Color color) {
        COLOR_HEADER_BG = color;
    }

    public static Color getCOLOR_HEADER_FG() {
        return COLOR_HEADER_FG;
    }

    public static void setCOLOR_HEADER_FG(Color color) {
        COLOR_HEADER_FG = color;
    }

    public static Color getCOLOR_BG_CHAT() {
        return COLOR_BG_CHAT;
    }

    public static void setCOLOR_BG_CHAT(Color color) {
        COLOR_BG_CHAT = color;
    }

    public static Color getCOLOR_INPUT_PANEL_BG() {
        return COLOR_INPUT_PANEL_BG;
    }

    public static void setCOLOR_INPUT_PANEL_BG(Color color) {
        COLOR_INPUT_PANEL_BG = color;
    }

    public static Color getCOLOR_SEND_BUTTON_BG() {
        return COLOR_SEND_BUTTON_BG;
    }

    public static void setCOLOR_SEND_BUTTON_BG(Color color) {
        COLOR_SEND_BUTTON_BG = color;
    }

    public static Color getCOLOR_SEND_BUTTON_FG() {
        return COLOR_SEND_BUTTON_FG;
    }

    public static void setCOLOR_SEND_BUTTON_FG(Color color) {
        COLOR_SEND_BUTTON_FG = color;
    }

    public static Color getCOLOR_BUBBLE_ME() {
        return COLOR_BUBBLE_ME;
    }

    public static void setCOLOR_BUBBLE_ME(Color color) {
        COLOR_BUBBLE_ME = color;
    }

    public static Color getCOLOR_BUBBLE_PEER() {
        return COLOR_BUBBLE_PEER;
    }

    public static void setCOLOR_BUBBLE_PEER(Color color) {
        COLOR_BUBBLE_PEER = color;
    }

    public static Color getCOLOR_BUBBLE_TEXT_ME() {
        return COLOR_BUBBLE_TEXT_ME;
    }

    public static void setCOLOR_BUBBLE_TEXT_ME(Color color) {
        COLOR_BUBBLE_TEXT_ME = color;
    }

    public static Color getCOLOR_BUBBLE_TEXT_PEER() {
        return COLOR_BUBBLE_TEXT_PEER;
    }

    public static void setCOLOR_BUBBLE_TEXT_PEER(Color color) {
        COLOR_BUBBLE_TEXT_PEER = color;
    }

    public static Color getCOLOR_GENERAL_BG() {
        return COLOR_GENERAL_BG;
    }

    public static void setCOLOR_GENERAL_BG(Color color) {
        COLOR_GENERAL_BG = color;
    }

    public static Color getCOLOR_CHECK() {
        return COLOR_CHECK;
    }

    public static void setCOLOR_CHECK(Color color) {
        COLOR_CHECK = color;
    }
}