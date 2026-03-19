package com.fernandev.chatp2p.view;

import com.fernandev.chatp2p.view.interfaces.IView;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;


public class ThemeManager {

    private static final ThemeManager INSTANCE = new ThemeManager();

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
                "1", "Esmeralda",
                new Color(0, 168, 132), Color.WHITE,
                new Color(236, 229, 221), new Color(240, 242, 245),
                new Color(0, 168, 132), Color.WHITE,
                new Color(220, 248, 198), new Color(255, 255, 255),
                Color.DARK_GRAY, Color.DARK_GRAY,
                new Color(240, 242, 245)));
        THEMES.put("2", new Theme(
                "2", "Océano",
                new Color(21, 101, 192), Color.WHITE,
                new Color(227, 242, 253), new Color(232, 240, 254),
                new Color(21, 101, 192), Color.WHITE,
                new Color(187, 222, 251), new Color(255, 255, 255),
                Color.DARK_GRAY, Color.DARK_GRAY,
                new Color(232, 240, 254)));
        THEMES.put("3", new Theme(
                "3", "Crepúsculo",
                new Color(106, 27, 154), Color.WHITE,
                new Color(243, 229, 245), new Color(237, 231, 246),
                new Color(106, 27, 154), Color.WHITE,
                new Color(225, 190, 231), new Color(255, 255, 255),
                Color.DARK_GRAY, Color.DARK_GRAY,
                new Color(237, 231, 246)));
        THEMES.put("4", new Theme(
                "4", "Noche",
                new Color(28, 28, 46), new Color(180, 180, 220),
                new Color(45, 45, 68), new Color(35, 35, 55),
                new Color(80, 140, 240), Color.WHITE,
                new Color(60, 80, 140), new Color(200, 220, 255),
                new Color(220, 220, 255), new Color(180, 200, 255),
                new Color(35, 35, 55)));
        THEMES.put("5", new Theme(
                "5", "Sakura",
                new Color(194, 24, 91), Color.WHITE,
                new Color(252, 228, 236), new Color(253, 236, 244),
                new Color(194, 24, 91), Color.WHITE,
                new Color(248, 187, 208), new Color(255, 255, 255),
                Color.DARK_GRAY, Color.DARK_GRAY,
                new Color(253, 236, 244)));
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


    public void applyTheme(String themeId, ChatUI view) {
        Theme theme = THEMES.get(themeId);
        if (theme == null) {
            System.out.println("[ThemeManager] Tema desconocido: " + themeId);
            return;
        }

        if (view == null) {
            System.out.println("[ThemeManager] No se encontró ventana para aplicar el tema.");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            applyToComponent(theme, view);
            view.repaintRightPanel();
            view.repaint();
            view.revalidate();
        });
    }


//    public void applyThemeToWindow(String themeId, Window window) {
//        Theme theme = THEMES.get(themeId);
//        if (theme == null)
//            return;
//        SwingUtilities.invokeLater(() -> {
//            applyToComponent(theme, window);
//            window.repaint();
//            window.revalidate();
//        });
//    }

    private void applyToComponent(Theme t, ChatUI view) {

          view.setCOLOR_HEADER_BG(t.headerBg);
          view.setCOLOR_HEADER_FG(t.headerFg);
          view.setCOLOR_BG_CHAT(t.chatBg);
          view.setCOLOR_INPUT_PANEL_BG(t.inputPanelBg);
          view.setCOLOR_SEND_BUTTON_BG(t.sendButtonBg);
          view.setCOLOR_SEND_BUTTON_FG(t.sendButtonFg);
          view.setCOLOR_BUBBLE_ME(t.bubbleMe);
          view.setCOLOR_BUBBLE_PEER(t.bubblePeer);
          view.setCOLOR_BUBBLE_TEXT_ME(t.bubbleTextMe);
          view.setCOLOR_BUBBLE_TEXT_PEER(t.bubbleTextPeer);
          view.setCOLOR_GENERAL_BG(t.generalBg);

//        String name = comp.getName() != null ? comp.getName() : "";
//
//        if ("theme-header".equals(name)) {
//            comp.setBackground(t.headerBg);
//            comp.setForeground(t.headerFg);
//        }
//        else if ("theme-chat-bg".equals(name)) {
//            comp.setBackground(t.chatBg);
//        }
//        else if ("theme-input-panel".equals(name)) {
//            comp.setBackground(t.inputPanelBg);
//        }
//        else if ("theme-send-btn".equals(name)) {
//            comp.setBackground(t.sendButtonBg);
//            comp.setForeground(t.sendButtonFg);
//        }
//        else if ("theme-general-bg".equals(name)) {
//            comp.setBackground(t.generalBg);
//        }
//
//        if (comp instanceof Container) {
//            for (Component child : ((Container) comp).getComponents()) {
//                applyToComponent(t, child);
//            }
//        }
    }
}
