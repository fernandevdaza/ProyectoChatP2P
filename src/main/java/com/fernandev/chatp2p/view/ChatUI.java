/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.fernandev.chatp2p.view;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.command.*;
import com.fernandev.chatp2p.model.network.SocketClient;
import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.view.interfaces.IView;
import com.fernandev.chatp2p.view.panel.LeftPanel;
import com.fernandev.chatp2p.view.panel.RightPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.ConnectException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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

    public void setInputEnabledInRightPanel(boolean enabled) {
        this.rightPanel.setInputEnabled(enabled);
    }

    @Override
    public void onLoad(List<Peer> peers) {

        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                listModel.clear();
                for (Peer p : peers) {
                    if (p.getIsSelf() == 0) {
                        String ip = p.getLastIpAddr();
                        if (ip == null || ip.isBlank()) {
                            System.out.println("[DB] Peer sin IP válida: " + p.getId());
                            continue;
                        }
                        p.setConnected(false);
                        listModel.addElement(p);
                    }
                }
                leftPanel.setListModel(listModel);
            });

            for (Peer p : peers) {
                if (p.getIsSelf() == 0) {
                    String ip = p.getLastIpAddr();
                    if (ip == null || ip.isBlank())
                        continue;

                    try {
                        ConnectionController.getInstance().sendHelloToPeer(ip);
                    } catch (Exception ex) {
                        System.out.println("[HELLO] No se pudo enviar hello a " + ip + ": " + ex.getMessage());
                    }
                }
            }

        }, "load-contacts-thread").start();

    }

    public void onDisconnect(String id) {
        this.leftPanel.updatePeerStatus(id, false);
    }

    public void onMessage(SocketClient socketClient, MessageProtocol messageProtocol) {
            if (messageProtocol instanceof Invitacion) {
                Invitacion invitacion = (Invitacion) messageProtocol;
                int respuesta = JOptionPane.showConfirmDialog(this, "Llego la invitacion: " + invitacion.getNombre());
                if (respuesta == JOptionPane.YES_OPTION) {
                    try {
                        Peer me = peerController.getMyself();

                        MessageProtocol aceptar = new Aceptar(me.getId(), me.getDisplayName());
                        ConnectionController.getInstance().sendMessage(aceptar, socketClient);

                        ConnectionController.getInstance().addConnection(((Invitacion) messageProtocol).getIdUsuario(),
                                socketClient);
                        peerController.savePeer(messageProtocol.getIp(), ((Invitacion) messageProtocol).getIdUsuario(),
                                ((Invitacion) messageProtocol).getNombre(),
                                ConnectionController.getInstance().getPort());

                        String conversationId = messageController.createConversation();
                        messageController.setPeerToConversation(conversationId,
                                ((Invitacion) messageProtocol).getIdUsuario());

                        Peer peer = Peer.builder()
                                .id(((Invitacion) messageProtocol).getIdUsuario())
                                .displayName(((Invitacion) messageProtocol).getNombre())
                                .isSelf(0)
                                .lastIpAddr(messageProtocol.getIp())
                                .lastPort(socketClient.getPort())
                                .lastSeenAt(LocalDateTime.now())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                        peer.setConnected(true);
                        listModel.addElement(peer);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                if (respuesta == JOptionPane.NO_OPTION) {
                    MessageProtocol rechazar = new Rechazar();
                    ConnectionController.getInstance().sendMessage(rechazar, socketClient);
                }
            }

            if (messageProtocol instanceof Aceptar) {
                try {
                    ConnectionController.getInstance().addConnection(((Aceptar) messageProtocol).getIdUsuario(), socketClient);
                    peerController.savePeer(messageProtocol.getIp(), ((Aceptar) messageProtocol).getIdUsuario(),
                            ((Aceptar) messageProtocol).getNombre(), ConnectionController.getInstance().getPort());
                    String conversationId = messageController.createConversation();
                    messageController.setPeerToConversation(conversationId, ((Aceptar) messageProtocol).getIdUsuario());

                    Peer peer = Peer.builder()
                            .id(((Aceptar) messageProtocol).getIdUsuario())
                            .displayName(((Aceptar) messageProtocol).getNombre())
                            .isSelf(0)
                            .lastIpAddr(messageProtocol.getIp())
                            .lastPort(socketClient.getPort())
                            .lastSeenAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    peer.setConnected(true);
                    listModel.addElement(peer);

                    JOptionPane.showMessageDialog(this,
                            ((Aceptar) messageProtocol).getNombre() + " aceptó la conexión.");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            if (messageProtocol instanceof Rechazar) {
                JOptionPane.showMessageDialog(this, ((Rechazar) messageProtocol).getIp() + " rechazó la conexión.");
                socketClient.close();
            }

            if (messageProtocol instanceof Mensaje) {
                try {
                    Mensaje msg = (Mensaje) messageProtocol;
                    String conversationId = messageController.getConversationIdByPeerId(msg.getIdUser());
                    messageController.saveMessage(msg.getIdMessage(), conversationId, msg.getIdUser(),
                            msg.getMessage());

                    this.rightPanel.addMessage(msg.getMessage(), false, msg.getIdUser());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if (messageProtocol instanceof Hello) {
                try {
                    if (peerController.getPeerById(((Hello) messageProtocol).getIdUser()) != null) {
                        Peer me = peerController.getMyself();
                        HelloAccept helloAccept = new HelloAccept(me.getId());
                        ConnectionController.getInstance().sendMessage(helloAccept, socketClient);
                        ConnectionController.getInstance().addConnection(((Hello) messageProtocol).getIdUser(), socketClient);
                        this.leftPanel.updatePeerStatus(((Hello) messageProtocol).idUser, true);
                    } else {
                        HelloReject helloReject = new HelloReject();
                        ConnectionController.getInstance().sendMessage(helloReject, socketClient);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if (messageProtocol instanceof HelloAccept) {
                ConnectionController.getInstance().addConnection(((HelloAccept) messageProtocol).getIdUser(), socketClient);
                this.leftPanel.updatePeerStatus(((HelloAccept) messageProtocol).idUser, true);
            }
            if (messageProtocol instanceof HelloReject) {
                JOptionPane.showMessageDialog(this, ((HelloReject) messageProtocol).getIp() + " rechazó la conexión.");
                socketClient.close();
            }
            if (messageProtocol instanceof Offline) {
                String userName = peerController.getPeerNameByIp(messageProtocol.getIp());
                JOptionPane.showMessageDialog(this, userName + " está en modo Offline!");
            }

    }

}