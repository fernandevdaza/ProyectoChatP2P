package com.fernandev.chatp2p.view.panel;

import com.fernandev.chatp2p.controller.ConnectionMediator;
import com.fernandev.chatp2p.model.entities.command.Invitacion;
import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.network.SocketClient;
import com.fernandev.chatp2p.model.repository.PeerDao;
import com.fernandev.chatp2p.view.BubbleBubble;
import com.fernandev.chatp2p.view.BubbleData;
import com.fernandev.chatp2p.view.ChatUI;
import com.fernandev.chatp2p.view.Contact;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class LeftPanel extends JPanel {
    private JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private JButton connectButton = new JButton("Nueva conexión (+)");
    private JButton offlineModeButton = new JButton("Modo offline");

    private DefaultListModel<Peer> listModel;
    private JList<Peer> contactList;

    private BubbleBubble bubbleBubble;
    private java.util.List<BubbleData> messageHistory = new java.util.ArrayList<>();


    private ChatUI mainView;
    private static final Color COLOR_HEADER = new Color(0, 168, 132);
    private final ImageIcon iconOnline = new ImageIcon(
            new ImageIcon("/home/fernandev/Coding/Cliente2/src/main/java/com/fernandev/chatp2p/view/resources/online.png")
            .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)
    );

    private final ImageIcon iconOffline = new ImageIcon(
            new ImageIcon("/home/fernandev/Coding/Cliente2/src/main/java/com/fernandev/chatp2p/view/resources/offline.png")
            .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)
    );


    public LeftPanel(ChatUI ui, DefaultListModel<Peer> list){
        this.listModel = list;
        this.mainView = ui;
        this.setLayout(new BorderLayout());

        buildHeader();
        buildPeerList();
        buildConnectButton();
        buildOfflineModeButton();

        this.setPreferredSize(new Dimension(300, 0));
        this.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
//        this.add(header, BorderLayout)
        this.add(new JScrollPane(contactList), BorderLayout.CENTER);
        this.add(offlineModeButton, BorderLayout.NORTH);
        this.add(connectButton, BorderLayout.SOUTH);
    }



    private void buildHeader(){
        header.setBackground(new Color(240, 242, 245));
        header.setPreferredSize(new Dimension(300, 60));

        JLabel labelUser = new JLabel("Mi usuario");
        labelUser.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(labelUser);
    }

    private void buildPeerList(){
        contactList = new JList<>(listModel);
        contactList.setFixedCellHeight(50);
        contactList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        contactList.setCellRenderer((list, c, index, isSelected, cellHasFocus) -> {
            JPanel panel1 = new JPanel(new BorderLayout(10, 0));
            panel1.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));

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

            if (isSelected) lblText.setForeground(list.getSelectionForeground());

            panel1.add(lblText, BorderLayout.CENTER);

            return panel1;
        });


        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()){
                Peer selection = contactList.getSelectedValue();
                if (selection != null) {
                    String id = selection.getId();

                    mainView.setContactSelectedConnected(selection.getConnected());

                    setContactSelected(true, id);
                    loadSelectedChat(id);
                }
            }
        });
    }

    private void buildConnectButton(){
        connectButton.setBackground(COLOR_HEADER);
        connectButton.setForeground(Color.WHITE);
        connectButton.addActionListener(e -> {
            connectButton.setEnabled(false);
            String ip = JOptionPane.showInputDialog(this, "Ingresa IP:");
            if (ip != null && !ip.isEmpty()) {
                new Thread(() -> {
                    try{
                        Peer me = ConnectionMediator.getInstance().getMyself();

                        SocketClient socketClient = ConnectionMediator.getInstance().connectToPeer(ip);
                        ConnectionMediator.getInstance().sendMessage(new Invitacion(me.getId(), me.getDisplayName()), socketClient);
                    }catch (Exception error){
                        System.out.println(error.getMessage());
                    }finally {
                        javax.swing.SwingUtilities.invokeLater(() -> connectButton.setEnabled(true));
                    }
                }).start();
            }
        });
    }

    private void buildOfflineModeButton(){
        offlineModeButton.setBackground(Color.RED);
        offlineModeButton.setForeground(Color.WHITE);
        offlineModeButton.addActionListener(e -> {
            offlineModeButton.setEnabled(false);
            new Thread(() -> {
                try{
                    ConnectionMediator.getInstance().onModeOffline();
                }catch (Exception error){
                    System.out.println(error.getMessage());
                }finally {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        offlineModeButton.setText("Modo Online");
                        offlineModeButton.setBackground(Color.GREEN);
                        offlineModeButton.setEnabled(true);
                    });
                }
            }).start();
            boolean isOffline = ConnectionMediator.getInstance().getOffline();
            if(!isOffline){
                JOptionPane.showMessageDialog(mainView, "Modo Offline Activado");
            }else{
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

    private void setContactSelectedConnected(boolean connected, String id){
        if (!Objects.equals(mainView.getCurrentChatId(), id)) return;
        SwingUtilities.invokeLater(() -> {
            mainView.setContactSelectedConnected(connected);
            mainView.setInputEnabledInRightPanel(connected);
        });
    }


    public void loadSelectedChat(String id) {
        mainView.setCurrentChatId(id);
        String conversationId = ConnectionMediator.getInstance().getConversationIdByPeerId(id);
        java.util.List<Message> messages = ConnectionMediator.getInstance().getConversationMessagesWithConversationId(conversationId);

        messageHistory.clear();

        if (messages != null){
            for (Message message: messages){
                String meId = PeerDao.getInstance().findMe().getId();
                boolean isMe = Objects.equals(meId, message.getSender_peer_id());
                messageHistory.add(new BubbleData(message.getText_content(), isMe));
            }

            for (BubbleData msg : messageHistory) {
                mainView.paintBubbleInRightPanel(msg.text, msg.isMe);
            }
        }

    }

    public void updatePeerStatus(String idUser, boolean online) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < listModel.getSize(); i++) {
                Peer p = listModel.getElementAt(i);
                if (Objects.equals(p.getId(), idUser)) {
                    p.setConnected(online);
                    listModel.set(i, p);
                    if (Objects.equals(mainView.getCurrentChatId(), p.getId())){
                        setContactSelectedConnected(online, p.getId());
                    }
                    break;
                }
            }
        });
    }

    public void setListModel(DefaultListModel<Peer> listModel){
        this.listModel = listModel;
    }

}
