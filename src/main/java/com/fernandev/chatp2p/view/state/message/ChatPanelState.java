package com.fernandev.chatp2p.view.state.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatPanelState {
    private String lastReceivedMessageId = null;
    private String lastReceivedMessageSenderId = null;
    private boolean isRepainting = false;
    private boolean isLoading = false;
    public ChatPanelState(){

    }
}
