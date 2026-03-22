package com.fernandev.chatp2p.view.interfaces;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.view.panel.right.MessageBubble;
import com.fernandev.chatp2p.view.panel.left.LeftPanel;
import com.fernandev.chatp2p.view.panel.right.RightPanel;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public interface IView {
    void renderPeers();
    void setRightPanel(RightPanel rightPanel);
    RightPanel getRightPanel();
    void setLeftPanel(LeftPanel leftPanel);
    LeftPanel getLeftPanel();
    void repaintRightPanel();
    void repaintLeftPanel();
    void onPinMessageReceived(boolean isVisible, String messageId);
    void onBuzz(String text);
}
