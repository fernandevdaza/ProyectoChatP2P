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
    void onLoad(List<Peer> peers);
    void setRightPanel(RightPanel rightPanel);
    RightPanel getRightPanel();
    void setLeftPanel(LeftPanel leftPanel);
    LeftPanel getLeftPanel();
    DefaultListModel<Peer> getPeerDefaultListModel();
    Map<String, List<BubbleData>> getChatHistory();
    Map<String, BubbleBubble> getBubblesByMessageId();
    void repaintRightPanel();
    void setPinMessage(boolean isVisible, String message, String messageId);
    boolean getShowPinnedMessage();
}
