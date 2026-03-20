package com.fernandev.chatp2p.view.state.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InputPanelState {
    private boolean isOneTimeMessageButtonClicked = false;
    private boolean isSendMessageButtonClicked = false;
    private boolean isSendMessageEnterKeyPressed = false;
    private boolean isSendImageButtonClicked = false;
    private String lastSentMessageId = null;

    public InputPanelState(){

    }
}
