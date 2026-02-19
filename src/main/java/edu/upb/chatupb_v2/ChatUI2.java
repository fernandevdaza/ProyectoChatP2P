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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author rlaredo
 */
public class ChatUI2 extends javax.swing.JFrame implements SocketListener {
    SocketClient client;

    private static final Color COLOR_HEADER = new Color(0, 168, 132); // Verde oscurito
    private static final Color COLOR_BG_CHAT = new Color(236, 229, 221); // Beige fondo
    private static final Color COLOR_MY_BUBBLE = new Color(220, 248, 198); // Verde claro (Mío)
    private static final Color COLOR_THEIR_BUBBLE = new Color(255, 255, 255); // Blanco (Ellos)

    private JPanel chatPanel;
    private JTextField messageInput;
    private JScrollPane scrollPane;
    DefaultListModel<Contact> listModel = new DefaultListModel<>();
    private Map<String, java.util.List<BubbleData>> chatHistory = new HashMap<>();
    private String currentChatIp = null;

    public ChatUI2() {
        setTitle("Chat P2P");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel leftPanel = createLeftPanel();
        add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.CENTER);
        cargarContactosGuardados();
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

        contactList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof Contact) {
                    Contact c = (Contact) value;
                    setText("<html><b>" + c.getName() + "</b> <br><span style='color:gray; font-size:9px'>" + c.getIp() + "</span></html>");

                    setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
                }
                return this;
            }
        });

        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()){
                Contact selection = contactList.getSelectedValue();
                if (selection != null){
                    String ip = selection.getIp();
                    if(ConnectionMediator.getInstance().getConnection(ip) == null){
                        new Thread (() -> {
                            try {
                                Peer me = ConnectionMediator.getInstance().getMyself();
                                String peerId = ConnectionMediator.getInstance().getPeerIdByIp(ip);
                                SocketClient socketClient = ConnectionMediator.getInstance().connectToPeer(ip, this);
                                MessageProtocol hello = new Hello(me.getId());
                                ConnectionMediator.getInstance().sendMessage(hello, socketClient);
                            } catch (IOException error) {
                                throw new RuntimeException(error);
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                        }).start();
                    }
                    cargarChat(ip);
                }
            }
        });

        panel.add(header, BorderLayout.NORTH);
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

                       SocketClient socketClient = ConnectionMediator.getInstance().connectToPeer(ip, this);
                       ConnectionMediator.getInstance().sendMessage(new Invitacion(me.getId(), me.getDisplayName()), socketClient);
                   }catch (Exception error){
                       System.out.println(error.getMessage());
                   }finally {
                       javax.swing.SwingUtilities.invokeLater(() -> connectButton.setEnabled(true));
                   }
                }).start();
            }
        });
        panel.add(connectButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(new Color(240, 242, 245));
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        JLabel labelContact = new JLabel("Chat actual");
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

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(240, 242, 245));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        messageInput = new JTextField();
        messageInput.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        messageInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 0),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        messageInput.addActionListener(e -> enviarMensaje());

        JButton buttonSend = new JButton("➤");
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

        return panel;
    }

    private void cargarChat(String ip) {
        this.currentChatIp = ip; // Actualizamos el estado

        // 1. Limpiar el panel visualmente
        chatPanel.removeAll();
        // (Volvemos a poner el pegamento invisible para empujar arriba)
        JPanel verticalGlue = new JPanel();
        verticalGlue.setOpaque(false);
        chatPanel.add(verticalGlue);

        // 2. Recuperar historial de la memoria
        java.util.List<BubbleData> historial = chatHistory.getOrDefault(ip, new java.util.ArrayList<>());

        // 3. Pintar de nuevo las burbujas guardadas
        for (BubbleData msg : historial) {
            // Usamos una versión interna de addMessage que solo pinta, no guarda
            pintarBurbuja(msg.text, msg.isMe);
        }

        // 4. Refrescar UI
        chatPanel.revalidate();
        chatPanel.repaint();

        // Actualizar título del header derecho
        // (Necesitarás convertir tu JLabel del header derecho en variable de clase para acceder aquí)
        // lblContact.setText("Chat con: " + ip);
    }

    private void enviarMensaje() {
        String texto = messageInput.getText().trim();
        if (texto.isEmpty()) return;

        // 1. Mostrar visualmente (Lado derecho - true)
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

        // 3. Limpiar
        messageInput.setText("");
        messageInput.requestFocus();
    }


    public void addMessage(String text, boolean isMe, String targetIp) {
        // 1. GUARDAR EN MEMORIA (Siempre, aunque no esté viendo el chat)
        chatHistory.putIfAbsent(targetIp, new java.util.ArrayList<>());
        chatHistory.get(targetIp).add(new BubbleData(text, isMe));

        // 2. PINTAR SOLO SI ESTOY VIENDO ESE CHAT
        if (targetIp.equals(currentChatIp)) {
            pintarBurbuja(text, isMe);
        } else {
            // Opcional: Aquí podrías poner un contador de "mensajes no leídos" en la lista izquierda
            System.out.println("Mensaje recibido de " + targetIp + " (en segundo plano)");
        }
    }

    // Mueve tu lógica visual antigua a este método privado
    private void pintarBurbuja(String text, boolean isMe) {
        // 1. Creamos un panel horizontal para la fila completa
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setOpaque(false); // Transparente para ver el fondo
        rowPanel.setBorder(new EmptyBorder(2, 10, 2, 10)); // Margen externo

        // 2. Instanciamos tu diseño de burbuja (Clase interna BubbleBubble)
        BubbleBubble bubble = new BubbleBubble(text, isMe);

        // 3. Magia de Alineación: Usamos "Glue" (Pegamento elástico)
        if (isMe) {
            // Si soy yo: Ponemos pegamento a la izquierda para empujar la burbuja a la DERECHA
            rowPanel.add(Box.createHorizontalGlue());
            rowPanel.add(bubble);
        } else {
            // Si es el otro: Ponemos la burbuja y luego pegamento para empujar a la IZQUIERDA
            rowPanel.add(bubble);
            rowPanel.add(Box.createHorizontalGlue());
        }

        // 4. Aseguramos que la fila ocupe el ancho disponible pero su propia altura
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowPanel.getPreferredSize().height));

        // 5. Agregamos al panel vertical del chat
        chatPanel.add(rowPanel);
        chatPanel.add(Box.createVerticalStrut(5)); // Espacio vertical entre mensajes

        // 6. Refrescamos para que aparezca
        chatPanel.revalidate();
        chatPanel.repaint(); // A veces necesario para asegurar el dibujado
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
            setLayout(new BorderLayout());
            setOpaque(false); // Importante para dibujar nosotros el fondo
            setBorder(new EmptyBorder(10, 15, 10, 12)); // Padding interno del texto

            JTextArea textArea = new JTextArea(text);
            textArea.setOpaque(false);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));

            // Calculamos ancho máximo de burbuja
            int maxWidth = 400;
            textArea.setSize(new Dimension(maxWidth, Short.MAX_VALUE));
            Dimension preferredSize = textArea.getPreferredSize();
            if (preferredSize.width > maxWidth) {
                preferredSize.width = maxWidth;
            }
            textArea.setPreferredSize(preferredSize);

            add(textArea, BorderLayout.CENTER);

            // Hora pequeña
            JLabel timeLbl = new JLabel(new SimpleDateFormat("HH:mm").format(new Date()));
            timeLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
            timeLbl.setForeground(Color.GRAY);
            timeLbl.setBorder(new EmptyBorder(5, 0, 0, 0));
            add(timeLbl, BorderLayout.SOUTH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(isMe ? COLOR_MY_BUBBLE : COLOR_THEIR_BUBBLE);

            // Dibujar rectángulo redondeado
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private void cargarContactosGuardados() {
        new Thread(() -> { // Hilo secundario para no congelar mientras carga
            try {
                // 1. Consultar a la base de datos
                java.util.List<Peer> peersGuardados = PeerDao.getInstance().findAll();

                // 2. Actualizar la UI en el hilo correcto
                SwingUtilities.invokeLater(() -> {
                    for (Peer p : peersGuardados) {
                        if (p.getIsSelf() == 0) {
                            Contact contacto = new Contact(p.getId(), p.getLastIpAddr(), p.getDisplayName());
                            listModel.addElement(contacto);
                        }
                    }
                });

            } catch (Exception e) {
                System.err.println("Error cargando contactos: " + e.getMessage());
            }
        }).start();
    }

    @Override
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

                        listModel.addElement(new Contact(((Invitacion) messageProtocol).getIdUsuario(), messageProtocol.getIp(), ((Invitacion) messageProtocol).getNombre()));

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
                    listModel.addElement(new Contact(((Aceptar) messageProtocol).getIdUsuario(), messageProtocol.getIp(), ((Aceptar) messageProtocol).getNombre()));
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

                        HelloAccept helloAccept = new HelloAccept();
                        ConnectionMediator.getInstance().sendMessage(helloAccept, socketClient);
                        ConnectionMediator.getInstance().addConnections(messageProtocol.getIp(), socketClient);
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
            }
            if (messageProtocol instanceof HelloReject){
                JOptionPane.showMessageDialog(this, ((HelloReject) messageProtocol).getIp() + " rechazó la conexión.");
                socketClient.close();
            }
        });

    }

}
