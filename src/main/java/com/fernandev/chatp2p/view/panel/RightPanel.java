package com.fernandev.chatp2p.view.panel;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.model.entities.protocol.messages.Mensaje;
import com.fernandev.chatp2p.model.entities.protocol.messages.MensajeUnico;
import com.fernandev.chatp2p.model.entities.protocol.messages.Zumbido;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.messages.CambiarTema;
import com.fernandev.chatp2p.model.entities.protocol.messages.MessageImage;
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
    private JPanel header = new JPanel(new BorderLayout());
    private JPanel pinMessageBox = new JPanel();
    private JPanel chatPanel = new JPanel();
    private JScrollPane scrollPane = new JScrollPane(chatPanel);
    private JPanel inputPanel;
    private JButton buttonSend = new JButton("➤");
    private JButton buttonImage = new JButton("📷");
    private JButton buttonOneTimeMessage = new JButton("❶");
    JButton buzzButton = new JButton("\uD83D\uDD0A");
    JButton themeButton = new JButton("Tema");
    private boolean isOneTimeMessage = false;
    private JTextField messageInput = new JTextField();
    private boolean showPinnedMessageBox = false;
    private String pinnedMessage = "";
    private String pinnedMessageId = "";
    private JLabel labelPinnedMessage;

    private BubbleBubble bubbleBubble;
    private ChatUI mainView;
    public RightPanel(ChatUI ui) {
        this.setLayout(new BorderLayout());

        this.mainView = ui;

        if (!mainView.getContactSelected()) {
            return;
        }

        buildPinMessageBox();
        buildHeader();
        buildChatPanel();
        buildScrollPane();
        buildMessageInput();
        buildSendButton();
        buildInputPanel();

        this.setPreferredSize(new Dimension(300, 0));
        this.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        JPanel northWrapper = new JPanel(new BorderLayout());
        northWrapper.add(header, BorderLayout.NORTH);
        northWrapper.add(pinMessageBox, BorderLayout.SOUTH);
        this.add(northWrapper, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(inputPanel, BorderLayout.SOUTH);

        setInputEnabled(mainView.getContactSelectedConected());

    }

    public void addImageMessage(String base64Image, boolean isMe, String targetId, String messageId) {
        if (targetId.equals(mainView.getCurrentChatId())) {
            paintBubbleImage(base64Image, isMe, messageId);
        } else {
            System.out.println("Imágen recibida de " + targetId + " (en segundo plano)");
        }
    }

    public void addMessage(String text, boolean isMe, String targetId, String messageId, boolean isOneTimeMessage) {
        mainView.getChatHistory().putIfAbsent(targetId, new java.util.ArrayList<>());
        mainView.getChatHistory().get(targetId).add(new BubbleData(text, isMe, messageId, isOneTimeMessage));

        if (targetId.equals(mainView.getCurrentChatId())) {
            paintBubble(text, isMe, messageId, false, isOneTimeMessage);
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

        addMessage(texto, true, mainView.getCurrentChatId(), uuid, isOneTimeMessage);

        new Thread(() -> {
            try {
                String targetId = mainView.getCurrentChatId();
                if (!isOneTimeMessage) {
                    Mensaje mensaje = new Mensaje();
                    mensaje.setIdMessage(uuid);
                    mensaje.setMessage(texto);
                    ConnectionController.getInstance().sendMessage(targetId, mensaje);
                } else {
                    MensajeUnico mensajeUnico = new MensajeUnico();
                    mensajeUnico.setIdMessage(uuid);
                    mensajeUnico.setMessage(texto);
                    ConnectionController.getInstance().sendMessage(targetId, mensajeUnico);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

        messageInput.setText("");
        messageInput.requestFocus();
    }

    private void setOneTimeMessage(boolean isOneTimeMessage) {
        this.isOneTimeMessage = isOneTimeMessage;
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
                MessageImage mensajeImagen = new MessageImage(
                        me.getId(), uuid, base64Image);

                String hostIp = ConnectionController.getInstance().getHostIpByPeerId(targetId);
                mensajeImagen.setIp(hostIp);

                ConnectionController.getInstance().sendMessage(targetId, mensajeImagen);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void buildHeader() {
        header.setBackground(mainView.getCOLOR_HEADER_BG());
        header.setForeground(mainView.getCOLOR_HEADER_FG());
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 50));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        header.setName("theme-header");

        String labelContactString = mainView.getPeerController().getPeerDisplayNameById(mainView.getCurrentChatId());
        JLabel labelContact = new JLabel("  " + labelContactString);
        labelContact.setFont(new Font("Segoe UI", Font.BOLD, 22));

        labelContact.setVerticalAlignment(JLabel.CENTER);
        labelContact.setHorizontalAlignment(JLabel.LEFT);

        buzzButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        buzzButton.setPreferredSize(new Dimension(50, 50));
        buzzButton.addActionListener(e -> {
            Zumbido zumbido = new Zumbido();
            ConnectionController.getInstance().sendMessage(mainView.getCurrentChatId(), zumbido);
        });

        if (!mainView.getContactSelectedConected()) {
            buzzButton.setEnabled(false);
            themeButton.setEnabled(false);
        }

        themeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        themeButton.setPreferredSize(new Dimension(100, 50));
        themeButton.setToolTipText("Cambiar tema");
        themeButton.setBackground(new Color(240, 242, 245));
        themeButton.setForeground(Color.DARK_GRAY);
        themeButton.setBorderPainted(true);
        themeButton.setFocusPainted(false);

        JPopupMenu themeMenu = new JPopupMenu();
        Map<String, String> themeEntries = com.fernandev.chatp2p.view.ThemeManager.getInstance().getThemeMenuEntries();
        for (Map.Entry<String, String> entry : themeEntries.entrySet()) {
            String themeId = entry.getKey();
            String label = entry.getValue();
            JMenuItem item = new JMenuItem(label);
            item.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            item.addActionListener(e -> {
                com.fernandev.chatp2p.view.ThemeManager.getInstance().applyTheme(themeId,
                        mainView);
                if (mainView.getContactSelectedConected()) {
                    new Thread(() -> {
                        try {
                            CambiarTema cmd = new CambiarTema();
                            cmd.setIdTema(themeId);
                            ConnectionController.getInstance().sendMessage(
                                    mainView.getCurrentChatId(), cmd);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                }
            });
            themeMenu.add(item);
        }

        themeButton.addActionListener(e -> themeMenu.show(themeButton, 0, themeButton.getHeight()));

        JPanel rightButtons = new JPanel(new BorderLayout());
        rightButtons.setOpaque(false);
        rightButtons.setPreferredSize(new Dimension(150, 50));
        rightButtons.add(themeButton, BorderLayout.WEST);
        rightButtons.add(buzzButton, BorderLayout.EAST);

        header.add(labelContact, BorderLayout.WEST);
        header.add(rightButtons, BorderLayout.EAST);
    }

    private void buildPinMessageBox() {
        pinMessageBox.setLayout(new BorderLayout(5, 0));
        pinMessageBox.setBackground(new Color(80, 80, 80));
        pinMessageBox.setPreferredSize(new Dimension(Integer.MAX_VALUE, 35));
        pinMessageBox.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.LIGHT_GRAY));

        JButton pin = new JButton("✕ Desfijar");
        pin.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pin.setForeground(Color.WHITE);
        pin.setBackground(new Color(113, 122, 122));
        pin.setBorderPainted(false);
        pin.setFocusPainted(false);
        pin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pin.setToolTipText("Desfijar mensaje");
        pin.addActionListener(e -> {
            MessageController.getInstance().pinMessage(pinnedMessageId, false);
        });
        pinMessageBox.add(pin, BorderLayout.WEST);

        labelPinnedMessage = new JLabel(pinnedMessage);
        labelPinnedMessage.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        labelPinnedMessage.setForeground(Color.white);
        pinMessageBox.add(labelPinnedMessage, BorderLayout.CENTER);

        pinMessageBox.setVisible(showPinnedMessageBox);
    }

    private void buildChatPanel() {
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(mainView.getCOLOR_BG_CHAT());

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
        buttonSend.setBackground(mainView.getCOLOR_SEND_BUTTON_BG());
        buttonSend.setForeground(mainView.getCOLOR_SEND_BUTTON_FG());
        buttonSend.setFont(new Font("Segoe UI", Font.BOLD, 20));
        buttonSend.setBorderPainted(false);
        buttonSend.setFocusPainted(false);
        buttonSend.setPreferredSize(new Dimension(60, 40));
        buttonSend.addActionListener(e -> enviarMensaje());

        buttonOneTimeMessage.setBackground(mainView.getCOLOR_INPUT_PANEL_BG());
        buttonOneTimeMessage.setForeground(Color.DARK_GRAY);
        buttonOneTimeMessage.setFont(new Font("Segoe UI", Font.BOLD, 20));
        buttonOneTimeMessage.setBorderPainted(false);
        buttonOneTimeMessage.setFocusPainted(false);
        buttonOneTimeMessage.setPreferredSize(new Dimension(60, 40));
        buttonOneTimeMessage.addActionListener(e -> {
            setOneTimeMessage(!isOneTimeMessage);
            if (isOneTimeMessage) {
                buttonOneTimeMessage.setBackground(new Color(135, 140, 145));
            } else {
                buttonOneTimeMessage.setBackground(mainView.getCOLOR_INPUT_PANEL_BG());
            }
        });

        buttonImage.setBackground(mainView.getCOLOR_INPUT_PANEL_BG());
        buttonImage.setForeground(mainView.getCOLOR_BUBBLE_TEXT_ME());
        buttonImage.setFont(new Font("Segoe UI", Font.BOLD, 20));
        buttonImage.setBorderPainted(false);
        buttonImage.setFocusPainted(false);
        buttonImage.setPreferredSize(new Dimension(60, 40));
        buttonImage.addActionListener(e -> enviarImagen());
    }

    private void buildInputPanel() {
        inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBackground(mainView.getCOLOR_INPUT_PANEL_BG());
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        inputPanel.add(buttonImage, BorderLayout.WEST);
        inputPanel.add(messageInput, BorderLayout.CENTER);

        JPanel sendButtonsPanel = new JPanel(new BorderLayout(5, 0));
        sendButtonsPanel.setBackground(mainView.getCOLOR_INPUT_PANEL_BG());
        sendButtonsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        sendButtonsPanel.add(buttonOneTimeMessage, BorderLayout.WEST);
        sendButtonsPanel.add(buttonSend, BorderLayout.EAST);

        inputPanel.add(sendButtonsPanel, BorderLayout.EAST);
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

    public void setShowPinnedMessageBox(boolean showPinnedMessageBox) {
        this.showPinnedMessageBox = showPinnedMessageBox;
        if (pinMessageBox != null) {
            pinMessageBox.setVisible(showPinnedMessageBox);
        }
    }

    public boolean getShowPinnedMessageBox() {
        return this.showPinnedMessageBox;
    }

    public String getPinnedMessageId() {
        return this.pinnedMessageId;
    }

    public JButton getBuzzButton() {
        return this.buzzButton;
    }

    public JButton getThemeButton() { return this.themeButton; }

    public void setPinnedMessageId(String pinnedMessageId) {
        this.pinnedMessageId = pinnedMessageId;
    }

    public void setPinnedMessage(String pinnedMessage) {
        this.pinnedMessage = pinnedMessage;
        if (labelPinnedMessage != null) {
            labelPinnedMessage.setText(pinnedMessage);
        }
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

    public void paintBubble(String text, boolean isMe, String messageId, boolean received, boolean isOneTimeMessage) {
        JPanel rowPanel = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        rowPanel.setOpaque(false);
        bubbleBubble = new BubbleBubble(text, isMe, messageId, isOneTimeMessage);

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
        bubbleBubble = new BubbleBubble(base64Image, isMe, true, messageId, false);

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
