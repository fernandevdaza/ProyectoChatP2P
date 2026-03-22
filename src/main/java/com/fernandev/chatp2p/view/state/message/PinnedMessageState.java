package com.fernandev.chatp2p.view.state.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PinnedMessageState {
    private boolean showPinnedMessageBox = false;
    private String pinnedMessageContent = null;
    private String pinnedMessageId = null;

    public PinnedMessageState(){

    }
}
