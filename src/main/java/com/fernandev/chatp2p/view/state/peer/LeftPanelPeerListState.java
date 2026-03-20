package com.fernandev.chatp2p.view.state.peer;

import com.fernandev.chatp2p.view.panel.left.LeftPanelPeerList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeftPanelPeerListState {
    private boolean isLoading = false;
    private boolean isPeerListRendered = false;
    private boolean isPeerItemClicked = false;

    public LeftPanelPeerListState(){

    }
}
