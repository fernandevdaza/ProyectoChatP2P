package com.fernandev.chatp2p.view.panel.right;

import com.fernandev.chatp2p.view.ChatUI;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.message.ChatPanelState;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

@Getter
@Setter
public class RightPanel extends JPanel implements StateListener {
    private JPanel header;
    private JPanel pinMessageBox;
    private JPanel chatPanel;
    private InputPanel inputPanel;
    private JPanel headerWrapper;
    private JScrollPane scrollPane;
    private StateManager stateManager = StateManager.getInstance();

    private MessageBubble messageBubble;
    private ChatUI mainView;

    public RightPanel() {
        stateManager.subscribeToState(this);

        this.setLayout(new BorderLayout());

        boolean isPeerSelected = stateManager.getCurrentState().getSelectedPeer().isSelected();
        if (!isPeerSelected) {
            return;
        }

        this.header = new RightPanelHeader();
        this.pinMessageBox = new PinnedMessageBox();
        this.chatPanel = new ChatPanel();
        this.inputPanel = new InputPanel();
        this.headerWrapper = new JPanel();

        buildHeaderWrapper();

        this.setPreferredSize(new Dimension(300, 0));
        this.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        this.add(headerWrapper, BorderLayout.NORTH);
        this.add(chatPanel, BorderLayout.CENTER);
        this.add(inputPanel, BorderLayout.SOUTH);

    }

    public void buildHeaderWrapper() {
        this.headerWrapper.setLayout(new BorderLayout());
        this.headerWrapper.add(this.header, BorderLayout.NORTH);
        this.headerWrapper.add(this.pinMessageBox, BorderLayout.SOUTH);
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    @Override
    public void onChange(State newState) {
        ChatPanelState chatPanelState = newState.getChatPanelState();

        if (chatPanelState.isRepainting()) {
            this.scrollToBottom();
            chatPanelState.setRepainting(false);
        }
        stateManager.setNewState(newState, List.of());
    }

    public void unsubscribeAll() {
        stateManager.unsubscribeToState(this);
        if (header instanceof StateListener)
            stateManager.unsubscribeToState((StateListener) header);
        if (pinMessageBox instanceof StateListener)
            stateManager.unsubscribeToState((StateListener) pinMessageBox);
        if (chatPanel instanceof StateListener)
            stateManager.unsubscribeToState((StateListener) chatPanel);
        if (inputPanel instanceof StateListener)
            stateManager.unsubscribeToState((StateListener) inputPanel);
    }
}
