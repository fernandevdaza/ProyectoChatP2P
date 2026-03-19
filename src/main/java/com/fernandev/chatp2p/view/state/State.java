package com.fernandev.chatp2p.view.state;

import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.view.BubbleBubble;
import com.fernandev.chatp2p.view.state.message.PinnedMessageState;
import com.fernandev.chatp2p.view.state.notification.NotificationState;
import com.fernandev.chatp2p.view.state.peer.SelectedPeerState;
import com.fernandev.chatp2p.view.state.theme.Theme;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class State {
    private SelectedPeerState selectedPeer = new SelectedPeerState();
    private List<Peer> peersList = new ArrayList<>();
    private boolean isPeerListRendered = false;
    private Map<String, List<Message>> chatHistory = new HashMap<>();
    private NotificationState notificationState = new NotificationState();
    private long lastBuzzMillis = 0;
    private PinnedMessageState pinnedMessage = new PinnedMessageState();
    private Theme theme = new Theme();

    public State(){

    }
}
