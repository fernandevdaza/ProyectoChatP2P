package com.fernandev.chatp2p.view.panel.right;

import com.fernandev.chatp2p.controller.ConnectionController;
import com.fernandev.chatp2p.controller.PeerController;
import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.entities.protocol.messages.MessageImage;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.message.InputPanelState;
import com.fernandev.chatp2p.view.state.theme.rightpanel.ChatInputPanelTheme;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SendImageButton extends JButton implements StateListener {

    private StateManager stateManager = StateManager.getInstance();
    private ChatInputPanelTheme theme;


    public SendImageButton(){

        applyTheme();

        this.setText("📷");
        this.setBackground(theme.getCOLOR_IMAGE_BUTTON_BG());
        this.setForeground(theme.getCOLOR_IMAGE_BUTTON_FG());
        this.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        this.setPreferredSize(new Dimension(60, 40));
        this.addActionListener(e -> {
            State state = stateManager.getCurrentState();
            InputPanelState inputPanelState = state.getInputPanelState();
            inputPanelState.setSendImageButtonClicked(true);
            stateManager.setNewState(state, List.of(InputPanel.class));
        });
    }


    public void applyTheme(){
        this.theme = stateManager.getCurrentState().getTheme().getRightPanelTheme().getChatInputPanelTheme();
    }

    @Override
    public void onChange(State newState) {

    }
}
