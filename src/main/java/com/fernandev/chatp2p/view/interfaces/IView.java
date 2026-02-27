package com.fernandev.chatp2p.view.interfaces;
import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.view.panel.LeftPanel;
import com.fernandev.chatp2p.view.panel.RightPanel;

import javax.swing.*;
import java.util.List;

public interface IView {
    void onLoad(List<Peer> peers);
    void setRightPanel(RightPanel rightPanel);
    RightPanel getRightPanel();
    void setLeftPanel(LeftPanel leftPanel);
    LeftPanel getLeftPanel();
}
