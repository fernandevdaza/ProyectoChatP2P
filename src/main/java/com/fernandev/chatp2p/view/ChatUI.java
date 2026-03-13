/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.fernandev.chatp2p.view;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
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

    DefaultListModel<Peer> listModel = new DefaultListModel<>();

    public ChatUI() {
        setTitle("Chat P2P");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        this.leftPanel = new LeftPanel(this, listModel);
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

    public void setPeerController(PeerController peerController) {
        this.peerController = peerController;
    }

    public PeerController getPeerController() {
        return this.peerController;
    }

    public void setMessageController(MessageController messageController) {
        this.messageController = messageController;
    }

    public MessageController getMessageController() {
        return this.messageController;
    }

    public void paintBubbleInRightPanel(String text, boolean isMe) {
        this.rightPanel.paintBubble(text, isMe);
    }

    public void paintBubbleInRightPanel(String text, boolean isMe, String messageId, boolean received) {
        this.rightPanel.paintBubble(text, isMe, messageId, received);
    }

    public void setInputEnabledInRightPanel(boolean enabled) {
        this.rightPanel.setInputEnabled(enabled);
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
                listModel.clear();

                listModel.addAll(validPeers);

                leftPanel.setListModel(listModel);
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

    public void onUpdatePeerStatus(String id) {
        this.leftPanel.updatePeerStatus(id, false);
    }

    public void onDisconnect(String ip) {
        String message = "Conexión con cliente de ip: " + ip + " cerrada";
        javax.swing.SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message));
    }

    public boolean onInvitationReceived(String peerId, String nombre) {
        int respuesta = JOptionPane.showConfirmDialog(this, "Llegó la invitación: " + nombre);
        return respuesta == JOptionPane.YES_OPTION;
    }

    public void onInvitationAccepted(String peerId, String nombre, String ip, int peerPort) {
        Peer peer = Peer.builder()
                .id(peerId)
                .displayName(nombre)
                .isSelf(0)
                .lastIpAddr(ip)
                .lastPort(peerPort)
                .lastSeenAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        peer.setConnected(true);
        SwingUtilities.invokeLater(() -> listModel.addElement(peer));
    }

    public void onInvitationRejected(String ip) {
        javax.swing.SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, ip + " rechazó la conexión."));
    }

    public void onChatMessage(String peerId, String idMessage, String message) {
        SwingUtilities.invokeLater(() -> this.rightPanel.addMessage(message, false, peerId));
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

}