package com.fernandev.chatp2p.view.panel;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.model.entities.db.MessageStatusType;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.view.BubbleData;
import com.fernandev.chatp2p.view.ChatUI;
import com.fernandev.chatp2p.view.panel.left.ConnectButton;
import com.fernandev.chatp2p.view.panel.left.LeftPanelHeader;
import com.fernandev.chatp2p.view.panel.left.LeftPanelPeerList;
import com.fernandev.chatp2p.view.panel.notification.NotificationButton;
import com.fernandev.chatp2p.view.panel.notification.NotificationPanel;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.notification.NotificationState;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class LeftPanel extends JPanel implements StateListener {
    private JPanel header;

    private StateManager stateManager = StateManager.getInstance();
    private ConnectButton connectButton;
    private LeftPanelPeerList peerList;

    private JButton offlineModeButton = new JButton("Modo offline");

    private ChatUI mainView;

    public LeftPanel(ChatUI ui) {
        StateManager.getInstance().subscribeToState(this);

        this.mainView = ui;
        this.header = new LeftPanelHeader();
        this.connectButton = new ConnectButton();
        this.peerList = new LeftPanelPeerList();

        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(300, 0));
        this.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        this.add(header, BorderLayout.NORTH);
        this.add(new JScrollPane(peerList), BorderLayout.CENTER);
        this.add(connectButton, BorderLayout.SOUTH);
    }


//
//    private void buildOfflineModeButton() {
//        offlineModeButton.setBackground(Color.RED);
//        offlineModeButton.setForeground(Color.WHITE);
//        offlineModeButton.addActionListener(e -> {
//            offlineModeButton.setEnabled(false);
//            new Thread(() -> {
//                try {
//                    ConnectionController.getInstance().onModeOffline();
//                } catch (Exception error) {
//                    System.out.println(error.getMessage());
//                } finally {
//                    javax.swing.SwingUtilities.invokeLater(() -> {
//                        offlineModeButton.setText("Modo Online");
//                        offlineModeButton.setBackground(Color.GREEN);
//                        offlineModeButton.setEnabled(true);
//                    });
//                }
//            }).start();
//            boolean isOffline = ConnectionController.getInstance().getOffline();
//            if (!isOffline) {
//                JOptionPane.showMessageDialog(mainView, "Modo Offline Activado");
//            } else {
//                javax.swing.SwingUtilities.invokeLater(() -> {
//                    offlineModeButton.setText("Modo Offline");
//                    offlineModeButton.setBackground(Color.RED);
//                });
//                JOptionPane.showMessageDialog(mainView, "Modo Offline desactivado");
//            }
//        });
//    }

    public void loadSelectedChat(String id) {
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
                messageHistory
                        .add(new BubbleData(message.getTextContent(), isMe, message.getId(), message.getIsEphemeral()));
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


    public void addNotification(String text) {
        State state = stateManager.getCurrentState();
        NotificationState notificationState = state.getNotificationState();
        List<String> notifications = notificationState.getNotifications();
        int unreadNotificationsCount = notificationState.getUnreadNotificationsCount();

        notifications.addFirst(text);

        notificationState.setUnreadNotificationsCount(unreadNotificationsCount + 1);

        stateManager.setNewState(state, List.of(NotificationButton.class, NotificationPanel.class));
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

    @Override
    public void onChange(State newState) {

    }
}
