package com.fernandev.chatp2p.view.panel;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.controller.exception.UnreachableException;
import com.fernandev.chatp2p.model.entities.protocol.messages.Hello;
import com.fernandev.chatp2p.model.entities.protocol.messages.Invitacion;
import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.model.entities.db.MessageStatusType;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.view.BubbleData;
import com.fernandev.chatp2p.view.ChatUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LeftPanel extends JPanel {
    private JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private JButton connectButton = new JButton("Nueva conexión (+)");
    private JButton offlineModeButton = new JButton("Modo offline");
    private JButton notificationButton = new JButton("🔔");
    private JPopupMenu notificationPopupMenu = new JPopupMenu();
    private JPanel notificationPanel = new JPanel();
    private JList<Peer> contactList;

    private ChatUI mainView;
    private static final Color COLOR_HEADER = new Color(0, 168, 132);
    private ImageIcon iconOnline;
    private ImageIcon iconOffline;

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

    public LeftPanel(ChatUI ui) {
        this.mainView = ui;
        this.setLayout(new BorderLayout());
        this.iconOffline = loadIcon("/view/offline.png");
        this.iconOnline = loadIcon("/view/online.png");

        buildNotificationPanel();
        buildHeader();
        buildPeerList();
        buildConnectButton();
        buildOfflineModeButton();

        this.setPreferredSize(new Dimension(300, 0));
        this.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        this.add(new JScrollPane(contactList), BorderLayout.CENTER);
        this.add(header, BorderLayout.NORTH);
        this.add(connectButton, BorderLayout.SOUTH);
    }

    private void buildNotificationPanel(){
        notificationButton.setFocusPainted(false);
        notificationButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        notificationPopupMenu.setLayout(new BorderLayout());

        notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));
        notificationPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        notificationPanel.setBackground(ChatUI.getCOLOR_BUBBLE_PEER());

        JScrollPane scrollPane = new JScrollPane(notificationPanel);
        scrollPane.setPreferredSize(new Dimension(240, 180));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        notificationPopupMenu.add(scrollPane, BorderLayout.CENTER);

        notificationButton.addActionListener(e -> {
            refreshPopupNotification();
            notificationPopupMenu.show(notificationButton, notificationButton.getWidth() - 240, notificationButton.getHeight());
            mainView.setUnreadNotificationsCount(0);
            updateNotificationButtonText();
        });

        updateNotificationButtonText();
    }

    private void buildHeader() {
        header.setBackground(ChatUI.getCOLOR_GENERAL_BG());
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(300, 30));
        Peer me = PeerController.getInstance().getMyself();

        JLabel labelUser = new JLabel("Bienvenido, " + me.getDisplayName() + "!");
        labelUser.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelUser.setHorizontalAlignment(JLabel.CENTER);
        labelUser.setVerticalAlignment(JLabel.CENTER);
        header.add(labelUser, BorderLayout.WEST);
        header.add(notificationButton, BorderLayout.EAST);
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
                    // loadSelectedChat(id);
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
                                    // ConnectionController.getInstance().sendHelloToPeer(ip);
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
        connectButton.setBackground(ChatUI.getCOLOR_SEND_BUTTON_BG());
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
        mainView.getRightPanel().getBuzzButton().setEnabled(connected);
        mainView.getRightPanel().getThemeButton().setEnabled(connected);
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
                if (message.getStatus() != MessageStatusType.RECEIVED && !isMe) {
                    mainView.getMessageController().sendReceipt(message);
                    mainView.getMessageController().setReceived(message);
                }
                messageHistory.add(new BubbleData(message.getTextContent(), isMe, message.getId(), message.getIsEphemeral()));
                if (message.getIsFixed()) {
                    mainView.getRightPanel().setPinnedMessageId(message.getId());
                    mainView.getRightPanel().setPinnedMessage(message.getTextContent());
                    mainView.getRightPanel().setShowPinnedMessageBox(message.getIsFixed());
                }
            }

            for (BubbleData msg : messageHistory) {
                boolean received = msg.isMe && msg.messageId != null
                        && MessageController.getInstance().hasReceipt(msg.messageId);
                mainView.paintBubbleInRightPanel(msg.text, msg.isMe, msg.messageId, received, msg.getIsEphemeral());
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

    private void refreshPopupNotification() {
        notificationPanel.removeAll();

        if (mainView.getNotifications().isEmpty()) {
            JLabel emptyLabel = new JLabel("No hay notificaciones");
            emptyLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
            notificationPanel.add(emptyLabel);
        } else {
            for (String notification : mainView.getNotifications()) {
                JPanel item = createNotificationItem(notification);
                notificationPanel.add(item);
                notificationPanel.add(Box.createVerticalStrut(6));
            }
        }

        notificationPanel.revalidate();
        notificationPanel.repaint();
    }

    private JPanel createNotificationItem(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ChatUI.getCOLOR_BUBBLE_PEER());
        panel.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel label = new JLabel("<html>" + text + "</html>");
        panel.add(label, BorderLayout.CENTER);

        return panel;
    }

    private void updateNotificationButtonText() {
        if (mainView.getUnreadNotificationsCount() > 0) {
            notificationButton.setText("🔔 (" + mainView.getUnreadNotificationsCount() + ")");
        } else {
            notificationButton.setText("🔔");
        }
    }

    public void addNotification(String text) {
        mainView.getNotifications().addFirst(text);
        mainView.setUnreadNotificationsCount(mainView.getUnreadNotificationsCount() + 1);
        updateNotificationButtonText();
        refreshPopupNotification();
    }

    public void triggerBuzz() {
        shakeWindow(mainView);
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

}
