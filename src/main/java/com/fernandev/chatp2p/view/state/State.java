package com.fernandev.chatp2p.view.state;

import com.fernandev.chatp2p.model.entities.db.Message;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.view.panel.right.MessageBubble;
import com.fernandev.chatp2p.view.state.message.ChatPanelState;
import com.fernandev.chatp2p.view.state.message.InputPanelState;
import com.fernandev.chatp2p.view.state.message.MessageBubbleState;
import com.fernandev.chatp2p.view.state.message.PinnedMessageState;
import com.fernandev.chatp2p.view.state.notification.NotificationState;
import com.fernandev.chatp2p.view.state.peer.LeftPanelPeerListState;
import com.fernandev.chatp2p.view.state.peer.SelectedPeerState;
import com.fernandev.chatp2p.view.state.theme.Theme;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class State {
    private SelectedPeerState selectedPeer = new SelectedPeerState();
    private Map<String, Peer> peerMap = new HashMap<>();
    private InputPanelState inputPanelState = new InputPanelState();
    private ChatPanelState chatPanelState = new ChatPanelState();
    private MessageBubbleState messageBubbleState = new MessageBubbleState();
    private Map<String, Map<String, Message>> messageMap = new HashMap<>();
    private Map<String, MessageBubble> messageBubbleMap = new HashMap<>();
    private NotificationState notificationState = new NotificationState();
    private long lastBuzzMillis = 0;
    private PinnedMessageState pinnedMessage = new PinnedMessageState();
    private LeftPanelPeerListState leftPanelPeerListState = new LeftPanelPeerListState();
    private Theme theme = new Theme();

    public State(){

    }
}
