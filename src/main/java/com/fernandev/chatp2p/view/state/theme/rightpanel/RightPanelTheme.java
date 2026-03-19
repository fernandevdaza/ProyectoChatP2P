package com.fernandev.chatp2p.view.state.theme.rightpanel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RightPanelTheme {
    RightPanelHeaderTheme headerTheme = new RightPanelHeaderTheme();
    ChatPanelTheme chatPanelTheme = new ChatPanelTheme();
    ChatInputPanelTheme chatInputPanelTheme = new ChatInputPanelTheme();
    BubbleMessageTheme bubbleMessageTheme = new BubbleMessageTheme();;

    public RightPanelTheme(){
    }
}
