package com.fernandev.chatp2p.view.panel.right;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.messages.Mensaje;
import com.fernandev.chatp2p.model.entities.protocol.messages.MensajeUnico;
import com.fernandev.chatp2p.model.entities.protocol.messages.MessageImage;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.message.InputPanelState;
import com.fernandev.chatp2p.view.state.peer.SelectedPeerState;
import com.fernandev.chatp2p.view.state.theme.rightpanel.ChatInputPanelTheme;
import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.util.UUID;
import java.util.List;

@Getter
@Setter
public class InputPanel extends JPanel implements StateListener {

    private StateManager stateManager = StateManager.getInstance();
    private ChatInputPanelTheme theme;
    private SendImageButton sendImageButton;
    private MessageTextField messageTextField;
    private SendOneTimeMessageButton sendOneTimeMessageButton;
    private SendMessageButton sendMessageButton;
    private JPanel sendButtonsPanel;

    public InputPanel() {
        stateManager.subscribeToState(this);

        boolean isPeerConnected = stateManager.getCurrentState().getSelectedPeer().isConnected();

        applyTheme();

        this.setLayout(new BorderLayout(5, 0));
        this.setBackground(theme.getCOLOR_INPUT_PANEL_BG());
        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        this.sendImageButton = new SendImageButton();
        this.sendMessageButton = new SendMessageButton();
        this.messageTextField = new MessageTextField();
        this.sendOneTimeMessageButton = new SendOneTimeMessageButton();

        this.add(this.sendImageButton, BorderLayout.WEST);
        this.add(this.messageTextField, BorderLayout.CENTER);

        sendButtonsPanel = new JPanel(new BorderLayout(5, 0));
        sendButtonsPanel.setBackground(theme.getCOLOR_INPUT_PANEL_BG());
        sendButtonsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        sendButtonsPanel.add(sendOneTimeMessageButton, BorderLayout.WEST);
        sendButtonsPanel.add(sendMessageButton, BorderLayout.EAST);

        this.enablePanel(isPeerConnected);
        this.add(sendButtonsPanel, BorderLayout.EAST);
    }

    private void enviarMensaje() {
        String texto = messageTextField.getText().trim();
        if (texto.isEmpty())
            return;

        String uuid = UUID.randomUUID().toString();

        new Thread(() -> {
            try {
                State state = stateManager.getCurrentState();
                InputPanelState inputPanelState = state.getInputPanelState();

                String peerId = state.getSelectedPeer().getPeerId();
                boolean isOneTimeMessageClicked = inputPanelState.isOneTimeMessageButtonClicked();

                if (!isOneTimeMessageClicked) {
                    Mensaje mensaje = new Mensaje();
                    mensaje.setIdMessage(uuid);
                    mensaje.setMessage(texto);
                    ConnectionController.getInstance().sendMessage(peerId, mensaje);
                } else {
                    MensajeUnico mensajeUnico = new MensajeUnico();
                    mensajeUnico.setIdMessage(uuid);
                    mensajeUnico.setMessage(texto);
                    ConnectionController.getInstance().sendMessage(peerId, mensajeUnico);
                }

                inputPanelState.setLastSentMessageId(uuid);

                stateManager.setNewState(state, List.of(ChatPanel.class));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

        messageTextField.setText("");
        messageTextField.requestFocus();
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
        State state = stateManager.getCurrentState();
        InputPanelState inputPanelState = state.getInputPanelState();
        String peerId = state.getSelectedPeer().getPeerId();

        new Thread(() -> {
            try {
                Peer me = PeerController.getInstance().getMyself();
                MessageImage mensajeImagen = new MessageImage(
                        me.getId(), uuid, base64Image);

                String hostIp = ConnectionController.getInstance().getHostIpByPeerId(peerId);
                mensajeImagen.setIp(hostIp);

                ConnectionController.getInstance().sendMessage(peerId, mensajeImagen);

                inputPanelState.setLastSentMessageId(uuid);

                stateManager.setNewState(state, List.of(ChatPanel.class));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void applyTheme() {
        this.theme = stateManager.getCurrentState().getTheme().getRightPanelTheme().getChatInputPanelTheme();
    }

    public void enablePanel(boolean enable) {

//        SwingUtilities.invokeLater(() -> {
            this.setVisible(enable);

            if (messageTextField != null) {
                messageTextField.setEnabled(enable);
            }
            if (sendMessageButton != null) {
                sendMessageButton.setEnabled(enable);
            }
            if (sendImageButton != null) {
                sendImageButton.setEnabled(enable);
            }
            if (sendOneTimeMessageButton != null) {
                sendOneTimeMessageButton.setEnabled(enable);
            }

            this.revalidate();
            this.repaint();
//        });

    }

    @Override
    public void onChange(State newState) {
        InputPanelState inputPanelState = newState.getInputPanelState();
        SelectedPeerState selectedPeerState = newState.getSelectedPeer();

        applyTheme();
        this.setBackground(theme.getCOLOR_INPUT_PANEL_BG());
        if (sendButtonsPanel != null) {
            sendButtonsPanel.setBackground(theme.getCOLOR_INPUT_PANEL_BG());
        }
        this.revalidate();
        this.repaint();

        enablePanel(selectedPeerState.isConnected());

        if (inputPanelState.isSendMessageButtonClicked() || inputPanelState.isSendMessageEnterKeyPressed()) {
            enviarMensaje();
        }
        if (inputPanelState.isSendImageButtonClicked()) {
            enviarImagen();
        }
    }
}
