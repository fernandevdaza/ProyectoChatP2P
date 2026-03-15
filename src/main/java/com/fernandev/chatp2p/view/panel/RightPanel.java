package com.fernandev.chatp2p.view.panel;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.model.entities.command.Mensaje;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.view.BubbleBubble;
import com.fernandev.chatp2p.view.BubbleData;
import com.fernandev.chatp2p.view.ChatUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;
import java.util.List;

public class RightPanel extends JPanel {
    private JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private JPanel chatPanel = new JPanel();
    private JScrollPane scrollPane = new JScrollPane(chatPanel);
    private JPanel inputPanel;
    private JButton buttonSend = new JButton("➤");
    private JButton buttonImage = new JButton("📷");
    private JTextField messageInput = new JTextField();

    private BubbleBubble bubbleBubble;
    private ChatUI mainView;
    private static final Color COLOR_HEADER = new Color(0, 168, 132);
    private static final Color COLOR_BG_CHAT = new Color(236, 229, 221);

    public RightPanel(ChatUI ui) {
        this.setLayout(new BorderLayout());

        this.mainView = ui;

        if (!mainView.getContactSelected()) {
            return;
        }

        buildHeader();
        buildChatPanel();
        buildScrollPane();
        buildMessageInput();
        buildSendButton();
        buildInputPanel();

        this.setPreferredSize(new Dimension(300, 0));
        this.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        this.add(header, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(inputPanel, BorderLayout.SOUTH);

        setInputEnabled(mainView.getContactSelectedConected());

    }

    public void addMessage(String text, boolean isMe, String targetId) {
        addMessage(text, isMe, targetId, null);
    }

    public void addImageMessage(String base64Image, boolean isMe, String targetId, String messageId) {
        if (targetId.equals(mainView.getCurrentChatId())) {
            paintBubbleImage(base64Image, isMe, messageId);
        } else {
            System.out.println("Imágen recibida de " + targetId + " (en segundo plano)");
        }
    }

    public void addMessage(String text, boolean isMe, String targetId, String messageId) {
        mainView.getChatHistory().putIfAbsent(targetId, new java.util.ArrayList<>());
        mainView.getChatHistory().get(targetId).add(new BubbleData(text, isMe, messageId));

        if (targetId.equals(mainView.getCurrentChatId())) {
            paintBubble(text, isMe, messageId);
        } else {
            System.out.println("Mensaje recibido de " + targetId + " (en segundo plano)");
        }
    }

    public void removeMessage(String messageId) {
        List<BubbleData> currentHistory = mainView.getChatHistory().get(mainView.getCurrentChatId());
        Iterator<BubbleData> iterator = currentHistory.iterator();

        while (iterator.hasNext()) {
            BubbleData bubbleData = iterator.next();
            if (Objects.equals(bubbleData.getMessageId(), messageId)) {
                iterator.remove();
            }
        }
    }

    public void markMessageReceived(String messageId) {
        BubbleBubble bubble = mainView.getBubblesByMessageId().get(messageId);
        if (bubble != null) {
            SwingUtilities.invokeLater(() -> bubble.setReceived(true));
        }
    }

    private void enviarMensaje() {
        String texto = messageInput.getText().trim();
        if (texto.isEmpty())
            return;

        String uuid = UUID.randomUUID().toString();

        addMessage(texto, true, mainView.getCurrentChatId(), uuid);

        new Thread(() -> {
            try {
                String targetId = mainView.getCurrentChatId();
                Mensaje mensaje = new Mensaje();
                mensaje.setIdMessage(uuid);
                mensaje.setMessage(texto);
                ConnectionController.getInstance().sendMessage(targetId, mensaje);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

        messageInput.setText("");
        messageInput.requestFocus();
    }

    private void enviarImagen() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes (JPG, PNG)", "jpg", "jpeg", "png"));
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                Image image = ImageIO.read(selectedFile);
                if (image == null) {
                    JOptionPane.showMessageDialog(this, "Formato de imagen no soportado.");
                    return;
                }

                int maxWidth = 600;
                int width = image.getWidth(null);
                int height = image.getHeight(null);
                if (width > maxWidth) {
                    float ratio = (float) maxWidth / width;
                    width = maxWidth;
                    height = (int) (height * ratio);
                    Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    new javax.swing.ImageIcon(resizedImage).getImage();

                    java.awt.image.BufferedImage bimage = new java.awt.image.BufferedImage(width, height,
                            java.awt.image.BufferedImage.TYPE_INT_RGB);
                    Graphics2D bGr = bimage.createGraphics();
                    bGr.drawImage(resizedImage, 0, 0, null);
                    bGr.dispose();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bimage, "jpg", baos);
                    byte[] imageBytes = baos.toByteArray();
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    procesarEnvioImagen(base64Image);
                } else {
                    java.awt.image.BufferedImage bimage = new java.awt.image.BufferedImage(width, height,
                            java.awt.image.BufferedImage.TYPE_INT_RGB);
                    Graphics2D bGr = bimage.createGraphics();
                    bGr.drawImage(image, 0, 0, null);
                    bGr.dispose();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bimage, "jpg", baos);
                    byte[] imageBytes = baos.toByteArray();
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    procesarEnvioImagen(base64Image);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error procesando la imagen.");
            }
        }
    }

    private void procesarEnvioImagen(String base64Image) {
        String uuid = UUID.randomUUID().toString();

        addImageMessage(base64Image, true, mainView.getCurrentChatId(), uuid);

        new Thread(() -> {
            try {
                Peer me = mainView.getPeerController().getMyself();
                String targetId = mainView.getCurrentChatId();
                com.fernandev.chatp2p.model.entities.command.MessageImage mensajeImagen = new com.fernandev.chatp2p.model.entities.command.MessageImage(
                        me.getId(), uuid, base64Image);

                String hostIp = ConnectionController.getInstance().getHostIpByPeerId(targetId);
                mensajeImagen.setIp(hostIp);

                ConnectionController.getInstance().sendMessageById(targetId, mensajeImagen);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void buildHeader() {
        header.setBackground(new Color(240, 242, 245));
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        String labelContactString = mainView.getPeerController().getPeerDisplayNameById(mainView.getCurrentChatId());
        JLabel labelContact = new JLabel(labelContactString);
        labelContact.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(labelContact);
    }

    private void buildChatPanel() {
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(COLOR_BG_CHAT);

        JPanel verticalGlue = new JPanel();
        verticalGlue.setOpaque(false);
        chatPanel.add(verticalGlue);
    }

    private void buildScrollPane() {
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    }

    private void buildMessageInput() {
        messageInput.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        messageInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 0),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        messageInput.addActionListener(e -> enviarMensaje());
    }

    private void buildSendButton() {
        buttonSend.setBackground(COLOR_HEADER);
        buttonSend.setForeground(Color.WHITE);
        buttonSend.setFont(new Font("Segoe UI", Font.BOLD, 20));
        buttonSend.setBorderPainted(false);
        buttonSend.setFocusPainted(false);
        buttonSend.setPreferredSize(new Dimension(60, 40));
        buttonSend.addActionListener(e -> enviarMensaje());

        buttonImage.setBackground(new Color(240, 242, 245));
        buttonImage.setForeground(Color.DARK_GRAY);
        buttonImage.setFont(new Font("Segoe UI", Font.BOLD, 20));
        buttonImage.setBorderPainted(false);
        buttonImage.setFocusPainted(false);
        buttonImage.setPreferredSize(new Dimension(60, 40));
        buttonImage.addActionListener(e -> enviarImagen());
    }

    private void buildInputPanel() {
        inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBackground(new Color(240, 242, 245));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        inputPanel.add(buttonImage, BorderLayout.WEST);
        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(buttonSend, BorderLayout.EAST);
    }

    public void setInputEnabled(boolean enabled) {

        if (inputPanel != null) {
            inputPanel.setVisible(enabled);
        }
        if (messageInput != null) {
            messageInput.setEnabled(enabled);
        }
        if (buttonSend != null) {
            buttonSend.setEnabled(enabled);
        }
        if (buttonImage != null) {
            buttonImage.setEnabled(enabled);
        }

        this.revalidate();
        this.repaint();
    }

    public void repaintChatPanel(JPanel rowPanel) {

        if (rowPanel != null) {
            chatPanel.add(rowPanel);
        }

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

    public void paintBubble(String text, boolean isMe, String messageId) {
        paintBubble(text, isMe, messageId, false);
    }

    public void paintBubble(String text, boolean isMe, String messageId, boolean received) {
        JPanel rowPanel = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        rowPanel.setOpaque(false);
        bubbleBubble = new BubbleBubble(text, isMe, messageId);

        if (received && isMe) {
            bubbleBubble.setReceived(true);
        }

        if (messageId != null && isMe) {
            mainView.getBubblesByMessageId().put(messageId, bubbleBubble);
        }

        rowPanel.add(bubbleBubble);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowPanel.getPreferredSize().height));
        repaintChatPanel(rowPanel);
    }

    public void paintBubbleImage(String base64Image, boolean isMe, String messageId) {
        JPanel rowPanel = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        rowPanel.setOpaque(false);
        bubbleBubble = new BubbleBubble(base64Image, isMe, true, messageId);

        if (isMe) {
            bubbleBubble.setReceived(false);
        }

        if (messageId != null && isMe) {
            mainView.getBubblesByMessageId().put(messageId, bubbleBubble);
        }

        rowPanel.add(bubbleBubble);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowPanel.getPreferredSize().height));
        repaintChatPanel(rowPanel);
    }

    public void setMainView(ChatUI mainView) {
        this.mainView = mainView;
    }
}
