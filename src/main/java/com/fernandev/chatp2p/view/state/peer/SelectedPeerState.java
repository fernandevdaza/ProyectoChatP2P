package com.fernandev.chatp2p.view.state.peer;

import com.fernandev.chatp2p.model.entities.db.Peer;
import lombok.*;

@Getter
@Setter
public class SelectedPeerState {
    private boolean isSelected = false;
    private boolean isConnected =  false;
    private String peerId = null;
    private Peer peer = null;

    public SelectedPeerState(){

    }
}
