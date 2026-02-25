/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.bl.message.*;
import edu.upb.chatupb_v2.bl.server.SocketClient;
import edu.upb.chatupb_v2.bl.server.SocketListener;
import edu.upb.chatupb_v2.mediator.ConnectionMediator;
import edu.upb.chatupb_v2.repository.Message;
import edu.upb.chatupb_v2.repository.MessageDAO;
import edu.upb.chatupb_v2.repository.Peer;
import edu.upb.chatupb_v2.repository.PeerDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;


public class ChatUI2 extends javax.swing.JFrame {

    SocketClient client;

    private static final Color COLOR_HEADER = new Color(0, 168, 132);
    private static final Color COLOR_BG_CHAT = new Color(236, 229, 221);
    private static final Color COLOR_MY_BUBBLE = new Color(220, 248, 198);
    private static final Color COLOR_THEIR_BUBBLE = new Color(255, 255, 255);

    private JPanel chatPanel;
    private JTextField messageInput;
    private JScrollPane scrollPane;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel inputPanel;
    private JButton buttonSend;
    DefaultListModel<Contact> listModel = new DefaultListModel<>();
    private Map<String, java.util.List<BubbleData>> chatHistory = new HashMap<>();
    private String currentChatIp = null;
    private boolean isContactSelected = false;
    private boolean isContactConnected = false;
    private final ImageIcon iconOnline = new ImageIcon(new ImageIcon("/home/fernandev/Coding/Cliente2/src/main/java/edu/upb/chatupb_v2/resources/online.png")
            .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

    private final ImageIcon iconOffline = new ImageIcon(new ImageIcon("/home/fernandev/Coding/Cliente2/src/main/java/edu/upb/chatupb_v2/resources/offline.png")
            .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));


    public ChatUI2() {
        setTitle("Chat P2P");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        this.leftPanel = createLeftPanel();
        add(leftPanel, BorderLayout.WEST);

        this.rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.CENTER);
        cargarContactosGuardados();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {

                try {
                    new Thread(() -> {
                        ConnectionMediator.getInstance().shutdown();
                    }).start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                dispose();
                System.exit(0);
            }
        });

    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(new Color(240, 242, 245));
        header.setPreferredSize(new Dimension(300, 60));

        JLabel labelUser = new JLabel("Mi usuario");
        labelUser.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(labelUser);

        JList<Contact> contactList = new JList<>(listModel);
        contactList.setFixedCellHeight(50);
        contactList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        contactList.setCellRenderer(new ListCellRenderer<Contact>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Contact> list, Contact c, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel panel = new JPanel(new BorderLayout(10, 0));
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));

                if (isSelected) {
                    panel.setBackground(list.getSelectionBackground());
                } else {
                    panel.setBackground(list.getBackground());
                }

                JLabel lblIcon = new JLabel(c.getOnline() ? iconOnline : iconOffline);
                panel.add(lblIcon, BorderLayout.WEST);

                JLabel lblText = new JLabel();
                lblText.setText("<html><div style='margin-left:5px;'>" +
                        "<b>" + c.getName() + "</b><br>" +
                        "<span style='color:gray; font-size:9px'>" + c.getIp() + "</span>" +
                        "</div></html>");

                if (isSelected) lblText.setForeground(list.getSelectionForeground());

                panel.add(lblText, BorderLayout.CENTER);

                return panel;
            }
        });


        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()){
                Contact selection = contactList.getSelectedValue();
                if (selection != null) {
                    String ip = selection.getIp();

                    this.isContactConnected = selection.getOnline();

                    setContactSelected(true, ip);
                    cargarChat(ip);
                }
            }
        });

        panel.add(new JScrollPane(contactList), BorderLayout.CENTER);

        JButton connectButton = new JButton("Nueva conexión (+)");
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
        JButton offlineModeButton = new JButton("Modo offline");
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
                JOptionPane.showMessageDialog(this, "Modo Offline Activado");
            }else{
                javax.swing.SwingUtilities.invokeLater(() -> {
                    offlineModeButton.setText("Modo Offline");
                    offlineModeButton.setBackground(Color.RED);
                });
                JOptionPane.showMessageDialog(this, "Modo Offline desactivado");
            }
        });

        panel.add(offlineModeButton, BorderLayout.NORTH);
        panel.add(connectButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        if (!isContactSelected){
            return  panel;
        }

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(new Color(240, 242, 245));
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        String labelContactString = ConnectionMediator.getInstance().getPeerDisplayNameByIp(currentChatIp);
        JLabel labelContact = new JLabel(labelContactString);
        labelContact.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(labelContact);
        panel.add(header, BorderLayout.NORTH);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(COLOR_BG_CHAT);

        JPanel verticalGlue = new JPanel();
        verticalGlue.setOpaque(false);
        chatPanel.add(verticalGlue);

        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(240, 242, 245));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        messageInput = new JTextField();
        messageInput.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        messageInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 0),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        messageInput.addActionListener(e -> enviarMensaje());

        buttonSend = new JButton("➤");
        buttonSend.setBackground(COLOR_HEADER);
        buttonSend.setForeground(Color.WHITE);
        buttonSend.setFont(new Font("Segoe UI", Font.BOLD, 20));
        buttonSend.setBorderPainted(false);
        buttonSend.setFocusPainted(false);
        buttonSend.setPreferredSize(new Dimension(60, 40));
        buttonSend.addActionListener(e -> enviarMensaje());

        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(buttonSend, BorderLayout.EAST);

        panel.add(inputPanel, BorderLayout.SOUTH);

        setInputEnabled(isContactConnected);



        return panel;
    }

    private void setInputEnabled(boolean enabled) {
        this.isContactConnected = enabled;

        if (inputPanel != null) {
            inputPanel.setVisible(enabled);
        }
        if (messageInput != null) {
            messageInput.setEnabled(enabled);
        }
        if (buttonSend != null) {
            buttonSend.setEnabled(enabled);
        }

        if (rightPanel != null) {
            rightPanel.revalidate();
            rightPanel.repaint();
        }
    }

    private void setContactSelected(boolean selected, String ip) {
        this.isContactSelected = selected;
        this.currentChatIp = ip;

        this.remove(this.rightPanel);

        this.rightPanel = createRightPanel();

        this.add(this.rightPanel, BorderLayout.CENTER);

        this.revalidate();
        this.repaint();
    }

    private void setContactSelectedConnected(boolean connected, String ip){
        if (!Objects.equals(this.currentChatIp, ip)) return;
        SwingUtilities.invokeLater(() -> setInputEnabled(connected));
    }



    private void cargarChat(String ip) {
        this.currentChatIp = ip;

        chatPanel.removeAll();

        JPanel verticalGlue = new JPanel();
        verticalGlue.setOpaque(false);
        chatPanel.add(verticalGlue);

        String peerId = ConnectionMediator.getInstance().getPeerIdByIp(ip);
        String conversationId = ConnectionMediator.getInstance().getConversationIdByPeerId(peerId);
        java.util.List<Message> messages = ConnectionMediator.getInstance().getConversationMessagesWithConversationId(conversationId);


        java.util.List<BubbleData> historial = new java.util.ArrayList<>();



        if (messages != null){
            for (Message message: messages){
                String meId = PeerDao.getInstance().findMe().getId();
                boolean isMe = Objects.equals(meId, message.getSender_peer_id());
                historial.add(new BubbleData(message.getText_content(), isMe));
            }


            for (BubbleData msg : historial) {
                pintarBurbuja(msg.text, msg.isMe);
            }
        }

        chatPanel.revalidate();
        chatPanel.repaint();

    }

    public void actualizarEstadoContacto(String idUser, boolean online) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < listModel.getSize(); i++) {
                Contact c = listModel.getElementAt(i);
                if (Objects.equals(c.getId(), idUser)) {
                    c.setOnline(online);
                    listModel.set(i, c);
                    if (Objects.equals(currentChatIp, c.getIp())){
                        setContactSelectedConnected(true, c.getIp());
                        cargarChat(currentChatIp);
                    }
                    break;
                }
            }
        });
    }

    private void enviarMensaje() {
        String texto = messageInput.getText().trim();
        if (texto.isEmpty()) return;

        addMessage(texto, true, currentChatIp);

        new Thread(() -> {
            try {
                Peer me = ConnectionMediator.getInstance().getMyself();
                String conversationId = ConnectionMediator.getInstance().getConversationIdByIp(currentChatIp);

                String uuid = UUID.randomUUID().toString();
                SocketClient socketClient = ConnectionMediator.getInstance().getConnection(currentChatIp);
                Mensaje mensaje = new Mensaje(me.getId(), uuid, texto);

                mensaje.setIp(socketClient.getHostIp());
                ConnectionMediator.getInstance().saveMessage(currentChatIp, uuid, conversationId, me.getId(), texto);
                ConnectionMediator.getInstance().sendMessage(mensaje, socketClient);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

        messageInput.setText("");
        messageInput.requestFocus();
    }


    public void addMessage(String text, boolean isMe, String targetIp) {
        chatHistory.putIfAbsent(targetIp, new java.util.ArrayList<>());
        chatHistory.get(targetIp).add(new BubbleData(text, isMe));

        if (targetIp.equals(currentChatIp)) {
            pintarBurbuja(text, isMe);
        } else {
            System.out.println("Mensaje recibido de " + targetIp + " (en segundo plano)");
        }
    }

    private void pintarBurbuja(String text, boolean isMe) {
        JPanel rowPanel = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        rowPanel.setOpaque(false);
        BubbleBubble bubble = new BubbleBubble(text, isMe);

        rowPanel.add(bubble);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowPanel.getPreferredSize().height));
        chatPanel.add(rowPanel);
        chatPanel.revalidate();
        chatPanel.repaint();
        scrollToBottom();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }


    private class BubbleBubble extends JPanel {
        private final String text;
        private final boolean isMe;

        public BubbleBubble(String text, boolean isMe) {
            this.text = text;
            this.isMe = isMe;

            setLayout(new BorderLayout(0, 2));
            setOpaque(false);
            setBorder(new EmptyBorder(8, 12, 8, 12));

            JTextArea textArea = new JTextArea(text);
            textArea.setOpaque(false);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));

            //Esto es para lo del tamanio said soy adri
            FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
            int textWidth = fm.stringWidth(text);
            int maxWidth = 350;

            if (textWidth > maxWidth) {
                // Mensaje largo
                textArea.setSize(new Dimension(maxWidth, Short.MAX_VALUE));
                Dimension d = textArea.getPreferredSize();
                textArea.setPreferredSize(new Dimension(maxWidth, d.height));
            } else {
                // Mensaje corto
                textArea.setSize(new Dimension(textWidth + 10, Short.MAX_VALUE));
                Dimension d = textArea.getPreferredSize();
                textArea.setPreferredSize(new Dimension(textWidth + 10, d.height));
            }

            add(textArea, BorderLayout.CENTER);

            JLabel timeLbl = new JLabel(new SimpleDateFormat("HH:mm").format(new Date()));
            timeLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
            timeLbl.setForeground(Color.GRAY);
            timeLbl.setHorizontalAlignment(SwingConstants.RIGHT);

            add(timeLbl, BorderLayout.SOUTH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(isMe ? COLOR_MY_BUBBLE : COLOR_THEIR_BUBBLE);

            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

            g2.dispose();
            super.paintComponent(g);
        }
    }

//    private void sendHellosToPeers(){
//        new Thread(() -> {
//            try {
//                java.util.List<Peer> peersGuardados = PeerDao.getInstance().findAll();
//                for (Peer p: peersGuardados){
//                }
//            } catch (ConnectException | SQLException e) {
//                throw new RuntimeException(e);
//            }
//
//        }).start();
//    }

    private void cargarContactosGuardados() {
        new Thread(() -> {
            final java.util.List<Peer> peersGuardados;
            try {
                peersGuardados = PeerDao.getInstance().findAll();
                System.out.println("[DB] Peers encontrados: " + peersGuardados.size());
            } catch (Exception e) {
                System.err.println("Error cargando contactos (DB): " + e.getMessage());
                e.printStackTrace();
                return;
            }

            SwingUtilities.invokeLater(() -> {
                listModel.clear();

                for (Peer p : peersGuardados) {
                    if (p.getIsSelf() == 0) {
                        String ip = p.getLastIpAddr();
                        if (ip == null || ip.isBlank()) {
                            System.out.println("[DB] Peer sin IP válida: " + p.getId());
                            continue;
                        }

                        Contact contacto = new Contact(p.getId(), ip, p.getDisplayName(), false);
                        listModel.addElement(contacto);
                    }
                }
            });

            for (Peer p : peersGuardados) {
                if (p.getIsSelf() == 0) {
                    String ip = p.getLastIpAddr();
                    if (ip == null || ip.isBlank()) continue;

                    try {
                        ConnectionMediator.getInstance().sendHelloToPeer(ip);
                    } catch (Exception ex) {
                        System.out.println("[HELLO] No se pudo enviar hello a " + ip + ": " + ex.getMessage());
                    }
                }
            }

        }, "load-contacts-thread").start();
    }

    public void onDisconnect(String id){
        actualizarEstadoContacto(id, false);
        String ip = ConnectionMediator.getInstance().getPeerIpById(id);
        if (Objects.equals(ip, currentChatIp)){
            setContactSelectedConnected(false, ip);
            cargarChat(ip);
        }
    }

    public void onMessage(SocketClient socketClient, MessageProtocol messageProtocol) {
        SwingUtilities.invokeLater(() -> {
            if(messageProtocol instanceof Invitacion){
                Invitacion invitacion = (Invitacion) messageProtocol;
                int respuesta = JOptionPane.showConfirmDialog(this, "Llego la invitacion: "+ invitacion.getNombre());
                if (respuesta == JOptionPane.YES_OPTION){
                    try {
                        Peer me = ConnectionMediator.getInstance().getMyself();

                        MessageProtocol aceptar = new Aceptar(me.getId(), me.getDisplayName());
                        ConnectionMediator.getInstance().sendMessage(aceptar, socketClient);

                        ConnectionMediator.getInstance().addConnections(((Invitacion) messageProtocol).getIp(), socketClient);
                        ConnectionMediator.getInstance().savePeer(messageProtocol.getIp(), ((Invitacion) messageProtocol).getIdUsuario(), ((Invitacion) messageProtocol).getNombre());

                        String conversationId = ConnectionMediator.getInstance().createConversation();
                        ConnectionMediator.getInstance().setPeerToConversation(conversationId, ((Invitacion) messageProtocol).getIdUsuario());

                        listModel.addElement(new Contact(((Invitacion) messageProtocol).getIdUsuario(), messageProtocol.getIp(), ((Invitacion) messageProtocol).getNombre(), true));

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                if (respuesta == JOptionPane.NO_OPTION){
                    MessageProtocol rechazar = new Rechazar();
                    ConnectionMediator.getInstance().sendMessage(rechazar, socketClient);
                }
            }

            if(messageProtocol instanceof Aceptar){
                try {
                    ConnectionMediator.getInstance().addConnections(messageProtocol.getIp(), socketClient);
                    ConnectionMediator.getInstance().savePeer(messageProtocol.getIp(), ((Aceptar) messageProtocol).getIdUsuario(), ((Aceptar) messageProtocol).getNombre());
                    String conversationId = ConnectionMediator.getInstance().createConversation();
                    ConnectionMediator.getInstance().setPeerToConversation(conversationId, ((Aceptar) messageProtocol).getIdUsuario());
                    listModel.addElement(new Contact(((Aceptar) messageProtocol).getIdUsuario(), messageProtocol.getIp(), ((Aceptar) messageProtocol).getNombre(), true));
                    JOptionPane.showMessageDialog(this,  ((Aceptar) messageProtocol).getNombre() + " aceptó la conexión.");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            if(messageProtocol instanceof Rechazar){
                JOptionPane.showMessageDialog(this, ((Rechazar) messageProtocol).getIp() + " rechazó la conexión.");
                socketClient.close();
            }

            if (messageProtocol instanceof Mensaje){
                try {
                    String conversationId = ConnectionMediator.getInstance().getConversationIdByPeerId(((Mensaje) messageProtocol).getIdUser());
                    ConnectionMediator.getInstance().saveMessage(messageProtocol.getIp(), ((Mensaje) messageProtocol).getIdMessage(), conversationId, ((Mensaje) messageProtocol).getIdUser(), ((Mensaje) messageProtocol).getMessage());
                    addMessage(((Mensaje) messageProtocol).getMessage(), false, messageProtocol.getIp());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if (messageProtocol instanceof Hello){
                try {
                    if(ConnectionMediator.getInstance().getPeerIdByIp(messageProtocol.getIp()) != null){
                        Peer me = ConnectionMediator.getInstance().getMyself();
                        HelloAccept helloAccept = new HelloAccept(me.getId());
                        ConnectionMediator.getInstance().sendMessage(helloAccept, socketClient);
                        ConnectionMediator.getInstance().addConnections(messageProtocol.getIp(), socketClient);
                        actualizarEstadoContacto(((Hello) messageProtocol).idUser, true);
                    }else{
                        HelloReject helloReject = new HelloReject();
                        ConnectionMediator.getInstance().sendMessage(helloReject, socketClient);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if (messageProtocol instanceof HelloAccept){
                ConnectionMediator.getInstance().addConnections(messageProtocol.getIp(), socketClient);
                actualizarEstadoContacto(((HelloAccept) messageProtocol).idUser, true);
            }
            if (messageProtocol instanceof HelloReject){
                JOptionPane.showMessageDialog(this, ((HelloReject) messageProtocol).getIp() + " rechazó la conexión.");
                socketClient.close();
            }
            if (messageProtocol instanceof Offline){
                try {
                    String userName = ConnectionMediator.getInstance().getPeerNameByIp(messageProtocol.getIp());
                    JOptionPane.showMessageDialog(this, userName + " está en modo Offline!");
                } catch (SQLException | ConnectException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }


}