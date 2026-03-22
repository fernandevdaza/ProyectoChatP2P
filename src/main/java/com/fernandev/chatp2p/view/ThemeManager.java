package com.fernandev.chatp2p.view;

import com.fernandev.chatp2p.view.interfaces.IView;
import com.fernandev.chatp2p.view.state.State;
import com.fernandev.chatp2p.view.state.StateManager;
import com.fernandev.chatp2p.view.state.theme.leftpanel.LeftPanelHeaderTheme;
import com.fernandev.chatp2p.view.state.theme.leftpanel.LeftPanelTheme;
import com.fernandev.chatp2p.view.state.theme.rightpanel.*;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

public class ThemeManager {

    private static final ThemeManager INSTANCE = new ThemeManager();

    private StateManager stateManager = StateManager.getInstance();

    private ThemeManager() {
    }

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    public static class Theme {
        public final String id;
        public final String name;
        public final Color headerBg;
        public final Color headerFg;
        public final Color chatBg;
        public final Color inputPanelBg;
        public final Color sendButtonBg;
        public final Color sendButtonFg;
        public final Color bubbleMe;
        public final Color bubblePeer;
        public final Color bubbleTextMe;
        public final Color bubbleTextPeer;
        public final Color generalBg;

        public Theme(String id, String name,
                Color headerBg, Color headerFg,
                Color chatBg, Color inputPanelBg,
                Color sendButtonBg, Color sendButtonFg,
                Color bubbleMe, Color bubblePeer,
                Color bubbleTextMe, Color bubbleTextPeer,
                Color generalBg) {
            this.id = id;
            this.name = name;
            this.headerBg = headerBg;
            this.headerFg = headerFg;
            this.chatBg = chatBg;
            this.inputPanelBg = inputPanelBg;
            this.sendButtonBg = sendButtonBg;
            this.sendButtonFg = sendButtonFg;
            this.bubbleMe = bubbleMe;
            this.bubblePeer = bubblePeer;
            this.bubbleTextMe = bubbleTextMe;
            this.bubbleTextPeer = bubbleTextPeer;
            this.generalBg = generalBg;
        }
    }

    private static final Map<String, Theme> THEMES = new LinkedHashMap<>();
    static {
        THEMES.put("1", new Theme(
                "1", "Esmeralda Clásico",
                new Color(0, 150, 136), Color.WHITE, // Header matches primary teal
                new Color(236, 229, 221), new Color(240, 242, 245), // Chat Bg, Input Bg
                new Color(0, 150, 136), Color.WHITE, // Send button
                new Color(220, 248, 198), new Color(255, 255, 255), // Bubbles
                new Color(48, 48, 48), new Color(48, 48, 48), // Bubble text
                new Color(240, 242, 245))); // General bg
        THEMES.put("2", new Theme(
                "2", "Océano Moderno",
                new Color(10, 100, 210), Color.WHITE,
                new Color(230, 240, 255), new Color(245, 248, 255),
                new Color(10, 100, 210), Color.WHITE,
                new Color(205, 225, 255), new Color(255, 255, 255),
                new Color(20, 40, 70), new Color(20, 40, 70),
                new Color(245, 248, 255)));
        THEMES.put("3", new Theme(
                "3", "Atardecer Púrpura",
                new Color(103, 58, 183), Color.WHITE,
                new Color(244, 238, 255), new Color(250, 247, 255),
                new Color(103, 58, 183), Color.WHITE,
                new Color(218, 204, 245), new Color(255, 255, 255),
                new Color(40, 20, 60), new Color(40, 20, 60),
                new Color(250, 247, 255)));
        THEMES.put("4", new Theme(
                "4", "Modo Oscuro Premium",
                new Color(32, 34, 37), new Color(240, 240, 240),
                new Color(47, 49, 54), new Color(54, 57, 63),
                new Color(88, 101, 242), Color.WHITE,
                new Color(88, 101, 242), new Color(64, 68, 75),
                Color.WHITE, new Color(230, 230, 230),
                new Color(32, 34, 37)));
        THEMES.put("5", new Theme(
                "5", "Sakura Vibrante",
                new Color(233, 30, 99), Color.WHITE,
                new Color(252, 238, 245), new Color(255, 248, 251),
                new Color(233, 30, 99), Color.WHITE,
                new Color(255, 195, 216), new Color(255, 255, 255),
                new Color(80, 10, 30), new Color(80, 10, 30),
                new Color(255, 248, 251)));
    }

    public Map<String, String> getThemeMenuEntries() {
        Map<String, String> result = new LinkedHashMap<>();
        for (Theme t : THEMES.values()) {
            result.put(t.id, t.id + " | " + t.name);
        }
        return result;
    }

    public Theme getTheme(String themeId) {
        return THEMES.get(themeId);
    }

    public void applyTheme(String themeId) {
        Theme theme = THEMES.get(themeId);
        if (theme == null) {
            System.out.println("[ThemeManager] Tema desconocido: " + themeId);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            applyToComponent(theme);
        });
    }

    private void applyToComponent(Theme theme) {

        State state = stateManager.getCurrentState();
        RightPanelTheme rightPanelTheme = state.getTheme().getRightPanelTheme();
        RightPanelHeaderTheme rightPanelHeaderTheme = rightPanelTheme.getHeaderTheme();
        ChatPanelTheme chatPanelTheme = rightPanelTheme.getChatPanelTheme();
        ChatInputPanelTheme chatInputPanelTheme = rightPanelTheme.getChatInputPanelTheme();
        BubbleMessageTheme bubbleMessageTheme = rightPanelTheme.getBubbleMessageTheme();

        LeftPanelTheme leftPanelTheme = state.getTheme().getLeftPanelTheme();
        LeftPanelHeaderTheme leftPanelHeaderTheme = leftPanelTheme.getHeaderTheme();

        rightPanelHeaderTheme.setCOLOR_HEADER_BG(theme.headerBg);
        rightPanelHeaderTheme.setCOLOR_HEADER_FG(theme.headerFg);
        chatPanelTheme.setCOLOR_BG_CHAT(theme.chatBg);
        chatInputPanelTheme.setCOLOR_INPUT_PANEL_BG(theme.inputPanelBg);
        chatInputPanelTheme.setCOLOR_SEND_BUTTON_BG(theme.sendButtonBg);
        chatInputPanelTheme.setCOLOR_SEND_BUTTON_FG(theme.sendButtonFg);
        bubbleMessageTheme.setCOLOR_BUBBLE_ME(theme.bubbleMe);
        bubbleMessageTheme.setCOLOR_BUBBLE_PEER(theme.bubblePeer);
        bubbleMessageTheme.setCOLOR_BUBBLE_TEXT_ME(theme.bubbleTextMe);
        bubbleMessageTheme.setCOLOR_BUBBLE_TEXT_PEER(theme.bubbleTextPeer);
        leftPanelHeaderTheme.setCOLOR_GENERAL_BG(theme.generalBg);

        state.getTheme().setThemeChanged(true);

        stateManager.setNewState( state, java.util.List.of(ChatUI.class));
    }
}
