package com.fernandev.chatp2p.view.state.theme.rightpanel;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class ChatInputPanelTheme {
    private Color COLOR_INPUT_PANEL_BG = new Color(240, 242, 245);
    private Color COLOR_INPUT_PANEL_FG = Color.DARK_GRAY;
    private Color COLOR_IMAGE_BUTTON_BG =new Color(240, 242, 245);
    private Color COLOR_IMAGE_BUTTON_FG = Color.DARK_GRAY;
    private Color COLOR_SEND_BUTTON_BG = new Color(0, 168, 132);
    private Color COLOR_SEND_BUTTON_FG = Color.WHITE;
    private Color COLOR_ONE_TIME_MESSAGE_BUTTON_BG = new Color(240, 242, 245);
    private Color COLOR_ONE_TIME_MESSAGE_BUTTON_FG = Color.DARK_GRAY;
    private Color COLOR_ONE_TIME_MESSAGE_BUTTON_PRESSED_BG = new Color(135, 140, 145);

    public ChatInputPanelTheme(){

    }
}
