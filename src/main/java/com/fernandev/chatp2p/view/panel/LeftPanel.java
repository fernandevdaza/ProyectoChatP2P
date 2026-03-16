package com.fernandev.chatp2p.view.panel;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.exception.UnreachableException;
import com.fernandev.chatp2p.model.entities.command.Hello;
import com.fernandev.chatp2p.model.entities.command.Invitacion;
import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.model.entities.db.MessageStatusType;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.view.BubbleBubble;
import com.fernandev.chatp2p.view.BubbleData;
import com.fernandev.chatp2p.view.ChatUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LeftPanel extends JPanel {
    private JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private JButton connectButton = new JButton("Nueva conexión (+)");
    private JButton offlineModeButton = new JButton("Modo offline");

    private JList<Peer> contactList;

    private ChatUI mainView;
    private static final Color COLOR_HEADER = new Color(0, 168, 132);
    private final ImageIcon iconOnline = new ImageIcon(
            new ImageIcon(getClass().getResource("/com/fernandev/chatp2p/view/resources/online.png"))
                    .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

    private final ImageIcon iconOffline = new ImageIcon(
            new ImageIcon(getClass().getResource("/com/fernandev/chatp2p/view/resources/offline.png"))
                    .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

    public LeftPanel(ChatUI ui) {
        this.mainView = ui;
        this.setLayout(new BorderLayout());

        buildHeader();
        buildPeerList();
        buildConnectButton();
        buildOfflineModeButton();

        this.setPreferredSize(new Dimension(300, 0));
        this.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        this.add(new JScrollPane(contactList), BorderLayout.CENTER);
        this.add(offlineModeButton, BorderLayout.NORTH);
        this.add(connectButton, BorderLayout.SOUTH);
    }

    private void buildHeader() {
        header.setBackground(new Color(240, 242, 245));
        header.setPreferredSize(new Dimension(300, 60));

        JLabel labelUser = new JLabel("Mi usuario");
        labelUser.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(labelUser);
    }

    private void buildPeerList() {
        contactList = new JList<>(mainView.getPeerDefaultListModel());
        contactList.setFixedCellHeight(50);
        contactList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        contactList.setCellRenderer((list, c, index, isSelected, cellHasFocus) -> {
            JPanel panel1 = new JPanel(new BorderLayout(10, 0));
            panel1.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));

            if (isSelected) {
                panel1.setBackground(list.getSelectionBackground());
            } else {
                panel1.setBackground(list.getBackground());
            }

            JLabel lblIcon = new JLabel(c.getConnected() ? iconOnline : iconOffline);
            panel1.add(lblIcon, BorderLayout.WEST);

            JLabel lblText = new JLabel();
            lblText.setText("<html><div style='margin-left:5px;'>" +
                    "<b>" + c.getDisplayName() + "</b><br>" +
                    "<span style='color:gray; font-size:9px'>" + c.getLastIpAddr() + "</span>" +
                    "</div></html>");

            if (isSelected)
                lblText.setForeground(list.getSelectionForeground());

            panel1.add(lblText, BorderLayout.CENTER);

            return panel1;
        });

        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Peer selection = contactList.getSelectedValue();
                if (selection != null) {
                    String id = selection.getId();

                    mainView.setContactSelectedConnected(selection.getConnected());

                    setContactSelected(true, id);
//                    loadSelectedChat(id);
                }
            }
        });

        contactList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Peer selection = contactList.getSelectedValue();
                    if (selection != null) {
                        String ip = selection.getLastIpAddr();
                        if (ip != null && !ip.isBlank()) {
                            new Thread(() -> {
                                try {
//                                    ConnectionController.getInstance().sendHelloToPeer(ip);
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

    private void buildConnectButton() {
        connectButton.setBackground(COLOR_HEADER);
        connectButton.setForeground(Color.WHITE);
        connectButton.addActionListener(e -> {
            connectButton.setEnabled(false);
            String ip = JOptionPane.showInputDialog(this, "Ingresa IP:");
            if (ip != null && !ip.isEmpty()) {
                new Thread(() -> {
                    try {
                        ConnectionController.getInstance().sendMessage(ip, new Invitacion());
                    } catch (UnreachableException ue) {
                        javax.swing.SwingUtilities
                                .invokeLater(() -> JOptionPane.showMessageDialog(this, ue.getMessage()));
                    } catch (Exception error) {
                        System.out.println(error.getMessage());
                    } finally {
                        javax.swing.SwingUtilities.invokeLater(() -> connectButton.setEnabled(true));
                    }
                }).start();
            } else {
                connectButton.setEnabled(true);
            }
        });
    }

    private void buildOfflineModeButton() {
        offlineModeButton.setBackground(Color.RED);
        offlineModeButton.setForeground(Color.WHITE);
        offlineModeButton.addActionListener(e -> {
            offlineModeButton.setEnabled(false);
            new Thread(() -> {
                try {
                    ConnectionController.getInstance().onModeOffline();
                } catch (Exception error) {
                    System.out.println(error.getMessage());
                } finally {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        offlineModeButton.setText("Modo Online");
                        offlineModeButton.setBackground(Color.GREEN);
                        offlineModeButton.setEnabled(true);
                    });
                }
            }).start();
            boolean isOffline = ConnectionController.getInstance().getOffline();
            if (!isOffline) {
                JOptionPane.showMessageDialog(mainView, "Modo Offline Activado");
            } else {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    offlineModeButton.setText("Modo Offline");
                    offlineModeButton.setBackground(Color.RED);
                });
                JOptionPane.showMessageDialog(mainView, "Modo Offline desactivado");
            }
        });
    }

    private void setContactSelected(boolean selected, String id) {
        mainView.setContactSelected(selected);
        mainView.setCurrentChatId(id);
        mainView.repaintRightPanel();

    }

    private void setContactSelectedConnected(boolean connected, String id) {
        if (!Objects.equals(mainView.getCurrentChatId(), id))
            return;
        SwingUtilities.invokeLater(() -> {
            mainView.setContactSelectedConnected(connected);
            mainView.setInputEnabledInRightPanel(connected);
        });
    }

    public void loadSelectedChat(String id) {
        mainView.setCurrentChatId(id);
        String conversationId = MessageController.getInstance().getConversationIdByPeerId(id);

        List<Message> messages = MessageController.getInstance()
                .getConversationMessagesWithConversationId(conversationId);

        List<BubbleData> messageHistory = mainView.getChatHistory().getOrDefault(id, new ArrayList<BubbleData>());
        messageHistory.clear();


        if (messages != null) {
            for (Message message : messages) {
                String meId = mainView.getPeerController().getMyself().getId();
                boolean isMe = Objects.equals(meId, message.getSenderPeerId());
                if(message.getStatus() != MessageStatusType.RECEIVED && !isMe){
                    mainView.getMessageController().sendReceipt(message);
                    mainView.getMessageController().setReceived(message);
                }
                messageHistory.add(new BubbleData(message.getTextContent(), isMe, message.getId()));
            }

            for (BubbleData msg : messageHistory) {
                boolean received = msg.isMe && msg.messageId != null
                        && MessageController.getInstance().hasReceipt(msg.messageId);
                mainView.paintBubbleInRightPanel(msg.text, msg.isMe, msg.messageId, received);
            }
        }

    }

    public void updatePeerStatus(String idUser, boolean online) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < (mainView.getPeerDefaultListModel()).getSize(); i++) {
                Peer p = mainView.getPeerDefaultListModel().getElementAt(i);
                if (Objects.equals(p.getId(), idUser)) {
                    p.setConnected(online);
                    (mainView.getPeerDefaultListModel()).set(i, p);
                    if (Objects.equals(mainView.getCurrentChatId(), p.getId())) {
                        setContactSelectedConnected(online, p.getId());
                    }
                    break;
                }
            }
        });
    }


}
