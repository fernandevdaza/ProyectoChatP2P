package com.fernandev.chatp2p.view.panel.right;

import com.fernandev.chatp2p.view.ChatUI;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateListener;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.message.ChatPanelState;
import com.fernandev.chatp2p.view.state.peer.SelectedPeerState;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

@Getter
@Setter
public class RightPanel extends JPanel implements StateListener {

    private final StateManager stateManager = StateManager.getInstance();

    private JPanel contentPanel;
    private JPanel emptyPanel;

    private JPanel header;
    private JPanel pinMessageBox;
    private ChatPanel chatPanel;
    private InputPanel inputPanel;
    private JPanel headerWrapper;

    private JScrollPane scrollPane;
    private MessageBubble messageBubble;
    private ChatUI mainView;

    private final CardLayout cardLayout = new CardLayout();

    private static final String EMPTY_CARD = "EMPTY";
    private static final String CONTENT_CARD = "CONTENT";

    public RightPanel() {
        stateManager.subscribeToState(this);

        this.setLayout(cardLayout);
        this.setPreferredSize(new Dimension(300, 0));
        this.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        buildEmptyPanel();
        buildContentPanel();

        this.add(emptyPanel, EMPTY_CARD);
        this.add(contentPanel, CONTENT_CARD);

        updateVisibility(stateManager.getCurrentState());
    }

    private void buildEmptyPanel() {
        emptyPanel = new JPanel(new BorderLayout());

        JLabel emptyLabel = new JLabel("Selecciona un contacto para comenzar", SwingConstants.CENTER);
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
    }

    private void buildContentPanel() {
        contentPanel = new JPanel(new BorderLayout());

        this.header = new RightPanelHeader();
        this.pinMessageBox = new PinnedMessageBox();
        this.chatPanel = new ChatPanel();
        this.inputPanel = new InputPanel();
        this.headerWrapper = new JPanel();

        buildHeaderWrapper();

        contentPanel.add(headerWrapper, BorderLayout.NORTH);
        contentPanel.add(chatPanel, BorderLayout.CENTER);
        contentPanel.add(inputPanel, BorderLayout.SOUTH);
    }

    public void buildHeaderWrapper() {
        this.headerWrapper.setLayout(new BorderLayout());
        this.headerWrapper.add(this.header, BorderLayout.NORTH);
        this.headerWrapper.add(this.pinMessageBox, BorderLayout.SOUTH);
    }

    private void updateVisibility(State state) {
        SelectedPeerState selectedPeerState = state.getSelectedPeer();
        boolean hasSelectedPeer =
                selectedPeerState != null &&
                        selectedPeerState.getPeerId() != null;

        if (hasSelectedPeer) {
            cardLayout.show(this, CONTENT_CARD);
        } else {
            resetConversationUI();
            cardLayout.show(this, EMPTY_CARD);
        }

        this.revalidate();
        this.repaint();
    }

    private void resetConversationUI() {
        if (chatPanel != null) {
            chatPanel.clearMessages();
        }

        if (inputPanel != null) {
            inputPanel.clearInput();
        }

        if (header instanceof RightPanelHeader rightPanelHeader) {
            rightPanelHeader.clearHeader();
        }

        if (pinMessageBox instanceof PinnedMessageBox pinnedMessageBox) {
            pinnedMessageBox.clearPinnedMessage();
        }
    }

    private void scrollToBottom() {
        if (scrollPane == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    @Override
    public void onChange(State newState) {
        ChatPanelState chatPanelState = newState.getChatPanelState();

        updateVisibility(newState);

        if (chatPanelState.isRepainting()) {
            scrollToBottom();
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