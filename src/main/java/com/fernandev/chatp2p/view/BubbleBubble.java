package com.fernandev.chatp2p.view;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.MessageController;
import com.fernandev.chatp2p.model.entities.command.EliminarMensaje;
import com.fernandev.chatp2p.model.entities.command.FijarMensaje;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import javax.imageio.ImageIO;

public class BubbleBubble extends JPanel {
    private final String text;
    private final boolean isMe;
    private final String idMessage;
    private final boolean isEphemeral;
    private JLabel checkLabel;
    private static final Color COLOR_MY_BUBBLE = new Color(220, 248, 198);
    private static final Color COLOR_THEIR_BUBBLE = new Color(255, 255, 255);
    private static final Color COLOR_CHECK = new Color(53, 162, 235);

    public BubbleBubble(String content, boolean isMe, String idMessage, boolean isEphemeral) {
        this(content, isMe, false, idMessage, isEphemeral);
    }

    public BubbleBubble(String content, boolean isMe, boolean isImage, String idMessage, boolean isEphemeral) {
        this.text = content;
        this.isMe = isMe;
        this.idMessage = idMessage;
        this.isEphemeral = isEphemeral;

        setLayout(new BorderLayout(0, 2));
        setOpaque(false);
        setBorder(new EmptyBorder(8, 12, 8, 12));

        if (isImage) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(content);
                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                Image img = ImageIO.read(bis);

                int maxWidth = 300;
                int maxHeight = 300;
                int imgWidth = img.getWidth(null);
                int imgHeight = img.getHeight(null);

                if (imgWidth > maxWidth || imgHeight > maxHeight) {
                    float widthRatio = (float) maxWidth / imgWidth;
                    float heightRatio = (float) maxHeight / imgHeight;
                    float ratio = Math.min(widthRatio, heightRatio);

                    int newWidth = (int) (imgWidth * ratio);
                    int newHeight = (int) (imgHeight * ratio);

                    img = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                    new ImageIcon(img).getImage();
                }

                JLabel imageLabel = new JLabel(new ImageIcon(img));
                imageLabel.setOpaque(false);
                add(imageLabel, BorderLayout.CENTER);
            } catch (Exception e) {
                e.printStackTrace();
                JLabel errorLbl = new JLabel("Error cargando imagen");
                errorLbl.setForeground(Color.RED);
                add(errorLbl, BorderLayout.CENTER);
            }
        } else if (isEphemeral){
            JButton openMessage = new JButton("❶ Mensaje");
            if (Objects.equals(text, "")){
                openMessage.setEnabled(false);
            }
            openMessage.setFont(new Font("Segoe UI", Font.BOLD, 16));
            openMessage.setSize(new Dimension(350, 350));
            if(!isMe){
                openMessage.addActionListener(e -> {
                    openMessage.setEnabled(false);
                    JOptionPane.showMessageDialog(null, this.text);
                    MessageController.getInstance().editMessage(this.idMessage, "");
                });
            }else{
                openMessage.setEnabled(false);
            }
            add(openMessage, BorderLayout.CENTER);
        }else {
            JTextArea textArea = new JTextArea(content);
            textArea.setOpaque(false);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));

            FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
            int textWidth = fm.stringWidth(content);
            int maxWidth = 350;

            if (textWidth > maxWidth) {
                textArea.setSize(new Dimension(maxWidth, Short.MAX_VALUE));
                Dimension d = textArea.getPreferredSize();
                textArea.setPreferredSize(new Dimension(maxWidth, d.height));
            } else {
                textArea.setSize(new Dimension(textWidth + 10, Short.MAX_VALUE));
                Dimension d = textArea.getPreferredSize();
                textArea.setPreferredSize(new Dimension(textWidth + 10, d.height));
            }

            add(textArea, BorderLayout.CENTER);
        }

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        bottomPanel.setOpaque(false);

        JLabel timeLbl = new JLabel(new SimpleDateFormat("HH:mm").format(new Date()));
        timeLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        timeLbl.setForeground(Color.GRAY);

        bottomPanel.add(timeLbl);

        if (isMe) {
            checkLabel = new JLabel("✓✓");
            checkLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
            checkLabel.setForeground(Color.LIGHT_GRAY);
            bottomPanel.add(checkLabel);
        }

        add(bottomPanel, BorderLayout.SOUTH);
        setupContextMenu();
    }

    public void setReceived(boolean received) {
        if (checkLabel != null) {
            checkLabel.setForeground(received ? COLOR_CHECK : Color.LIGHT_GRAY);
            checkLabel.repaint();
        }
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

    private void setupContextMenu(){
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem deleteItem = new JMenuItem("Eliminar para mí");
        deleteItem.addActionListener(e -> {
            MessageController.getInstance().deleteMessage(this.idMessage);
        });

        JMenuItem pinItem = new JMenuItem("Fijar mensaje");
        pinItem.addActionListener(e -> {
            String senderPeerId = MessageController.getInstance().getSenderPeerIdByMessageId(this.idMessage);
            FijarMensaje fijarMensaje = new FijarMensaje();
            fijarMensaje.setIdMessage(this.idMessage);
            ConnectionController.getInstance().sendMessage(senderPeerId, fijarMensaje);
            MessageController.getInstance().pinMessage(this.idMessage, true);
        });



        popupMenu.add(deleteItem);
        if(isMe){
            JMenuItem deleteEveryoneItem = new JMenuItem("Eliminar para todos");

            deleteEveryoneItem.addActionListener(e -> {
                String senderPeerId = MessageController.getInstance().getSenderPeerIdByMessageId(this.idMessage);
                EliminarMensaje eliminarMensaje = new EliminarMensaje();
                eliminarMensaje.setIdMessage(this.idMessage);
                ConnectionController.getInstance().sendMessage(senderPeerId, eliminarMensaje);
                MessageController.getInstance().deleteMessage(this.idMessage);
            });

            popupMenu.add(deleteEveryoneItem);
            

        }

        popupMenu.add(new JSeparator());
        popupMenu.add(pinItem);


        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }
            private void showMenu(MouseEvent e) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

    }

    public String getIdMessage(){
        return this.idMessage;
    }
}
