package com.fernandev.chatp2p.view.interfaces;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.view.BubbleBubble;
import com.fernandev.chatp2p.view.BubbleData;
import com.fernandev.chatp2p.view.panel.LeftPanel;
import com.fernandev.chatp2p.view.panel.RightPanel;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public interface IView {
    void renderPeers(List<Peer> peers);
    void setRightPanel(RightPanel rightPanel);
    RightPanel getRightPanel();
    void setLeftPanel(LeftPanel leftPanel);
    LeftPanel getLeftPanel();
    DefaultListModel<Peer> getPeerDefaultListModel();
    Map<String, List<BubbleData>> getChatHistory();
    Map<String, BubbleBubble> getBubblesByMessageId();
    void repaintRightPanel();
    void repaintLeftPanel();
    void onPinMessageReceived(boolean isVisible, String messageId);
    boolean getShowPinnedMessage();
    void onBuzz(String text);
}
