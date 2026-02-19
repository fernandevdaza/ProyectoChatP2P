//package edu.upb.chatupb_v2;
//
//import javax.swing.*;
//import javax.swing.border.EmptyBorder;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.geom.RoundRectangle2D;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//public class ChatUserInteface extends JFrame {
//
//    // --- Colores estilo WhatsApp ---
//    private static final Color COLOR_HEADER = new Color(0, 168, 132); // Verde oscurito
//    private static final Color COLOR_BG_CHAT = new Color(236, 229, 221); // Beige fondo
//    private static final Color COLOR_MY_BUBBLE = new Color(220, 248, 198); // Verde claro (Mío)
//    private static final Color COLOR_THEIR_BUBBLE = new Color(255, 255, 255); // Blanco (Ellos)
//
//    // Componentes globales
//    private JPanel chatAreaPanel; // Donde van los mensajes
//    private JTextField txtInput;
//    private JScrollPane scrollPane;
//
//    // TODO: Aquí deberías tener referencia a tu controlador o mediator
//    // private ConnectionMediator mediator;
//
//    public ChatUserInteface() {
//        setTitle("Chat P2P - WhatsApp Style");
//        setSize(1000, 700); // Tamaño tipo web
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setLocationRelativeTo(null);
//        setLayout(new BorderLayout());
//
//        // 1. Panel Izquierdo (Lista de Contactos)
//        JPanel leftPanel = createLeftPanel();
//        add(leftPanel, BorderLayout.WEST);
//
//        // 2. Panel Derecho (Chat principal)
//        JPanel rightPanel = createRightPanel();
//        add(rightPanel, BorderLayout.CENTER);
//    }
//
//    // --- Construcción del Panel Izquierdo (Contactos) ---
//    private JPanel createLeftPanel() {
//        JPanel panel = new JPanel(new BorderLayout());
//        panel.setPreferredSize(new Dimension(300, 0));
//        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
//
//        // Header Usuario
//        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        header.setBackground(new Color(240, 242, 245));
//        header.setPreferredSize(new Dimension(300, 60));
//        JLabel lblUser = new JLabel("👤 Mi Usuario");
//        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 16));
//        header.add(lblUser);
//
//        // Lista de Contactos (Mockup)
//        DefaultListModel<String> listModel = new DefaultListModel<>();
//        listModel.addElement("Juan Pérez (192.168.1.5)");
//        listModel.addElement("Maria Lopez (192.168.1.6)");
//        JList<String> contactList = new JList<>(listModel);
//        contactList.setFixedCellHeight(50);
//        contactList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//
//        panel.add(header, BorderLayout.NORTH);
//        panel.add(new JScrollPane(contactList), BorderLayout.CENTER);
//
//        // Botón conectar rápido abajo
//        JButton btnAdd = new JButton("Nueva Conexión (+)");
//        btnAdd.setBackground(COLOR_HEADER);
//        btnAdd.setForeground(Color.WHITE);
//        btnAdd.addActionListener(e -> {
//            String ip = JOptionPane.showInputDialog(this, "Ingresa IP:");
//            if(ip != null && !ip.isEmpty()) {
//                // TODO: Lógica de conectar aquí
//                listModel.addElement("Nuevo (" + ip + ")");
//            }
//        });
//        panel.add(btnAdd, BorderLayout.SOUTH);
//
//        return panel;
//    }
//
//    // --- Construcción del Panel Derecho (Chat) ---
//    private JPanel createRightPanel() {
//        JPanel panel = new JPanel(new BorderLayout());
//
//        // Header del Chat
//        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        header.setBackground(new Color(240, 242, 245));
//        header.setPreferredSize(new Dimension(0, 60));
//        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
//        JLabel lblContact = new JLabel("💬 Chat Actual");
//        lblContact.setFont(new Font("Segoe UI", Font.BOLD, 16));
//        header.add(lblContact);
//        panel.add(header, BorderLayout.NORTH);
//
//        // Área de Mensajes (El Scroll)
//        chatAreaPanel = new JPanel();
//        chatAreaPanel.setLayout(new BoxLayout(chatAreaPanel, BoxLayout.Y_AXIS)); // Vertical
//        chatAreaPanel.setBackground(COLOR_BG_CHAT);
//
//        // Truco: Panel invisible para empujar mensajes hacia arriba al inicio
//        JPanel verticalGlue = new JPanel();
//        verticalGlue.setOpaque(false);
//        chatAreaPanel.add(verticalGlue);
//
//        scrollPane = new JScrollPane(chatAreaPanel);
//        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Scroll más suave
//        panel.add(scrollPane, BorderLayout.CENTER);
//
//        // Área de Input (Abajo)
//        JPanel inputPanel = new JPanel(new BorderLayout());
//        inputPanel.setBackground(new Color(240, 242, 245));
//        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
//
//        txtInput = new JTextField();
//        txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 16));
//        txtInput.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(Color.WHITE, 0),
//                BorderFactory.createEmptyBorder(10, 10, 10, 10) // Padding interno
//        ));
//
//        // Listener para ENTER
//        txtInput.addActionListener(e -> enviarMensaje());
//
//        JButton btnSend = new JButton("➤");
//        btnSend.setBackground(COLOR_HEADER);
//        btnSend.setForeground(Color.WHITE);
//        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 20));
//        btnSend.setBorderPainted(false);
//        btnSend.setFocusPainted(false);
//        btnSend.setPreferredSize(new Dimension(60, 40));
//        btnSend.addActionListener(e -> enviarMensaje());
//
//        inputPanel.add(txtInput, BorderLayout.CENTER);
//        inputPanel.add(btnSend, BorderLayout.EAST);
//        panel.add(inputPanel, BorderLayout.SOUTH);
//
//        return panel;
//    }
//
//    // --- Lógica de Enviar Mensaje ---
//    private void enviarMensaje() {
//        String texto = txtInput.getText().trim();
//        if (texto.isEmpty()) return;
//
//        // 1. Mostrar visualmente (Lado derecho - true)
//        addMessage(texto, true);
//
//        // 2. TODO: ENVIAR POR SOCKET REALMENTE
//        // mediator.sendMessage(..., texto);
//
//        // 3. Limpiar
//        txtInput.setText("");
//        txtInput.requestFocus();
//    }
//
//    // --- Método Mágico: Agregar Burbuja al Chat ---
//    // Úsalo desde tu SocketListener con SwingUtilities.invokeLater()
//    public void addMessage(String text, boolean isMe) {
//        // Contenedor de la fila (ocupa todo el ancho)
//        JPanel rowPanel = new JPanel();
//        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
//        rowPanel.setOpaque(false); // Transparente
//        rowPanel.setBorder(new EmptyBorder(5, 10, 5, 10)); // Margen externo
//
//        // La burbuja en sí
//        BubbleBubble bubble = new BubbleBubble(text, isMe);
//
//        // Alineación
//        if (isMe) {
//            rowPanel.add(Box.createHorizontalGlue()); // Empuja a la derecha
//            rowPanel.add(bubble);
//        } else {
//            rowPanel.add(bubble);
//            rowPanel.add(Box.createHorizontalGlue()); // Empuja a la izquierda
//        }
//
//        chatAreaPanel.add(rowPanel);
//
//        // Espaciador vertical pequeño
//        chatAreaPanel.add(Box.createVerticalStrut(5));
//
//        // Refrescar y bajar el scroll
//        chatAreaPanel.revalidate();
//        chatAreaPanel.repaint();
//        scrollToBottom();
//    }
//
//    private void scrollToBottom() {
//        SwingUtilities.invokeLater(() -> {
//            JScrollBar vertical = scrollPane.getVerticalScrollBar();
//            vertical.setValue(vertical.getMaximum());
//        });
//    }
//
//    // --- CLASE INTERNA: La Burbuja Gráfica ---
//    // Esta clase dibuja el globito redondeado
//    private class BubbleBubble extends JPanel {
//        private final String text;
//        private final boolean isMe;
//
//        public BubbleBubble(String text, boolean isMe) {
//            this.text = text;
//            this.isMe = isMe;
//            setLayout(new BorderLayout());
//            setOpaque(false); // Importante para dibujar nosotros el fondo
//            setBorder(new EmptyBorder(10, 15, 10, 12)); // Padding interno del texto
//
//            JTextArea textArea = new JTextArea(text);
//            textArea.setOpaque(false);
//            textArea.setEditable(false);
//            textArea.setLineWrap(true);
//            textArea.setWrapStyleWord(true);
//            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
//
//            // Calculamos ancho máximo de burbuja
//            int maxWidth = 400;
//            textArea.setSize(new Dimension(maxWidth, Short.MAX_VALUE));
//            Dimension preferredSize = textArea.getPreferredSize();
//            if (preferredSize.width > maxWidth) {
//                preferredSize.width = maxWidth;
//            }
//            textArea.setPreferredSize(preferredSize);
//
//            add(textArea, BorderLayout.CENTER);
//
//            // Hora pequeña
//            JLabel timeLbl = new JLabel(new SimpleDateFormat("HH:mm").format(new Date()));
//            timeLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
//            timeLbl.setForeground(Color.GRAY);
//            timeLbl.setBorder(new EmptyBorder(5, 0, 0, 0));
//            add(timeLbl, BorderLayout.SOUTH);
//        }
//
//        @Override
//        protected void paintComponent(Graphics g) {
//            Graphics2D g2 = (Graphics2D) g.create();
//            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//            g2.setColor(isMe ? COLOR_MY_BUBBLE : COLOR_THEIR_BUBBLE);
//
//            // Dibujar rectángulo redondeado
//            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
//
//            g2.dispose();
//            super.paintComponent(g);
//        }
//    }
//
//    // Main para probar visualmente (bórralo cuando lo integres)
//    public static void main(String[] args) {
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {}
//
//        SwingUtilities.invokeLater(() -> {
//            WhatsAppUI ui = new WhatsAppUI();
//            ui.setVisible(true);
//
//            // Simular chat
//            ui.addMessage("Hola! ¿Pudiste avanzar con el proyecto?", false);
//            ui.addMessage("Sí, acabo de terminar la UI, quedó genial 😎", true);
//        });
//    }
//}
