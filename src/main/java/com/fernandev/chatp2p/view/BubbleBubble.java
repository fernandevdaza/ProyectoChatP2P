package com.fernandev.chatp2p.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BubbleBubble extends JPanel {
    private final String text;
    private final boolean isMe;
    private JLabel checkLabel;
    private static final Color COLOR_MY_BUBBLE = new Color(220, 248, 198);
    private static final Color COLOR_THEIR_BUBBLE = new Color(255, 255, 255);
    private static final Color COLOR_CHECK = new Color(53, 162, 235);

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

        FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
        int textWidth = fm.stringWidth(text);
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
}
