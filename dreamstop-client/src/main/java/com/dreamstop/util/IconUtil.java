package com.dreamstop.util;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.util.HashMap;
import java.util.Map;

/**
 * Production Vector Iconography Engine.
 * Replaces OS-dependent emoji glyphs with resolution-independent SVG vector graphics.
 */
public final class IconUtil {

    public enum IconType {
        GIFT,
        USERS,
        SETTINGS,
        EDIT,
        TRASH,
        PLUS,
        SIGN_OUT,
        SEARCH,
        CHECK,
        CLOSE,
        GPU,
        GAMEPAD,
        MONITOR,
        HEADPHONES,
        CARD,
        WALLET,
        FIRE,
        STAR,
        MOON,
        SUN,
        PALETTE,
        USER,
        SAVE,
        DIAMOND
    }

    private static final Map<IconType, String> PATHS = new HashMap<>();

    static {
        // Material Design crisp vector paths (viewBox 0 0 24 24)
        PATHS.put(IconType.EDIT,
                "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");

        PATHS.put(IconType.TRASH,
                "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");

        PATHS.put(IconType.PLUS,
                "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z");

        PATHS.put(IconType.GIFT,
                "M20 6h-2.18c.11-.31.18-.65.18-1 0-1.66-1.34-3-3-3-1.05 0-1.96.54-2.5 1.35l-.5.65-.5-.65C10.96 2.54 10.05 2 9 2 7.34 2 6 3.34 6 5c0 .35.07.69.18 1H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-5-2c.55 0 1 .45 1 1s-.45 1-1 1h-2v-1c0-.55.45-1 1-1zM9 4c.55 0 1 .45 1 1v1H8c0-.55.45-1 1-1zm11 15H4v-2h16v2zm0-5H4V8h5.08L7 10.83 8.62 12 11 8.76V14h2V8.76L15.38 12 17 10.83 14.92 8H20v6z");

        PATHS.put(IconType.USERS,
                "M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z");

        PATHS.put(IconType.SETTINGS,
                "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z");

        PATHS.put(IconType.SIGN_OUT,
                "M10.09 15.59L11.5 17l5-5-5-5-1.41 1.41L12.67 11H3v2h9.67l-2.58 2.59zM19 3H5c-1.11 0-2 .9-2 2v4h2V5h14v14H5v-4H3v4c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z");

        PATHS.put(IconType.SEARCH,
                "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z");

        PATHS.put(IconType.CHECK,
                "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");

        PATHS.put(IconType.CLOSE,
                "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");

        PATHS.put(IconType.GPU,
                "M6 2v2h2v4h8V4h2V2h-2v2h-2V2h-4v2H8V2H6zm0 16v2h2v-2h4v2h2v-2h2v2h2v-2h2v-2h-2v-4h2v-2h-2V8h2V6h-2v2h-2v8h-8v-8H6V6H4v2h2v4H4v2h2v4H4v2h2zM9 9h6v6H9V9z");

        PATHS.put(IconType.GAMEPAD,
                "M21 6H3c-1.1 0-2 .9-2 2v8c0 1.1.9 2 2 2h18c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-10 7H8v3H6v-3H3v-2h3V8h2v3h3v2zm4.5 2c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm3-3c-.83 0-1.5-.67-1.5-1.5S17.67 9 18.5 9s1.5.67 1.5 1.5-.67 1.5-1.5 1.5z");

        PATHS.put(IconType.MONITOR,
                "M20 3H4c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2h6l-2 3v1h8v-1l-2-3h6c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 13H4V5h16v11z");

        PATHS.put(IconType.HEADPHONES,
                "M12 3c-4.97 0-9 4.03-9 9v7c0 1.1.9 2 2 2h4v-8H5v-1c0-3.87 3.13-7 7-7s7 3.13 7 7v1h-4v8h4c1.1 0 2-.9 2-2v-7c0-4.97-4.03-9-9-9z");

        PATHS.put(IconType.CARD,
                "M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.1 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z");

        PATHS.put(IconType.WALLET,
                "M21 18v1c0 1.1-.9 2-2 2H5c-1.11 0-2-.9-2-2V5c0-1.1.89-2 2-2h14c1.1 0 2 .9 2 2v1h-9c-1.11 0-2 .9-2 2v8c0 1.1.89 2 2 2h9zm-9-2h10V8H12v8zm4-2.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z");

        PATHS.put(IconType.FIRE,
                "M19.48 12.35c-1.57-4.08-7.16-4.3-5.81-10.23.1-.44-.37-.78-.75-.55C9.29 3.71 6.68 8 8.87 13.62c.18.46-.15.98-.65.98H8.2c-.37 0-.7-.24-.81-.59C6.8 12.1 7 9.8 8.03 8.35c.19-.26-.04-.62-.35-.53-2.97.87-4.68 3.8-4.68 6.68 0 4.97 4.03 9 9 9s9-4.03 9-9c0-.79-.11-1.54-.32-2.25-.09-.32-.42-.48-.7-.35-.29.14-.38.45-.5.75z");

        PATHS.put(IconType.STAR,
                "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z");

        PATHS.put(IconType.MOON,
                "M12.3 2a10 10 0 0 0-1.9 19.8 10 10 0 0 0 9.8-9.8c0-.5-.1-1-.2-1.5A8 8 0 0 1 12.3 2z");

        PATHS.put(IconType.SUN,
                "M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zM2 13h2c.55 0 1-.45 1-1s-.45-1-1-1H2c-.55 0-1 .45-1 1s.45 1 1 1zm18 0h2c.55 0 1-.45 1-1s-.45-1-1-1h-2c-.55 0-1 .45-1 1s.45 1 1 1zM11 2v2c0 .55.45 1 1 1s1-.45 1-1V2c0-.55-.45-1-1-1s-1 .45-1 1zm0 18v2c0 .55.45 1 1 1s1-.45 1-1v-2c0-.55-.45-1-1-1s-1 .45-1 1zM5.99 4.58a.996.996 0 0 0-1.41 0 .996.996 0 0 0 0 1.41l1.06 1.06c.39.39 1.03.39 1.41 0s.39-1.03 0-1.41L5.99 4.58zm12.37 12.37a.996.996 0 0 0-1.41 0 .996.996 0 0 0 0 1.41l1.06 1.06c.39.39 1.03.39 1.41 0s.39-1.03 0-1.41l-1.06-1.06zm1.06-10.96a.996.996 0 0 0 0-1.41.996.996 0 0 0-1.41 0l-1.06 1.06c-.39.39-.39 1.03 0 1.41s1.03.39 1.41 0l1.06-1.06zM7.05 18.36a.996.996 0 0 0 0-1.41.996.996 0 0 0-1.41 0l-1.06 1.06c-.39.39-.39 1.03 0 1.41s1.03.39 1.41 0l1.06-1.06z");

        PATHS.put(IconType.PALETTE,
                "M12 3c-4.97 0-9 4.03-9 9 0 2.12.74 4.07 1.97 5.61L4.35 19c-.39.39-.39 1.02 0 1.41.39.39 1.02.39 1.41 0l1.9-1.9C9.23 19.34 10.57 20 12 20c4.97 0 9-4.03 9-9 0-4.97-4.03-9-9-9zm-5.5 9c-.83 0-1.5-.67-1.5-1.5S5.67 9 6.5 9 8 9.67 8 10.5 7.33 12 6.5 12zm3-4C8.67 8 8 7.33 8 6.5S8.67 5 9.5 5s1.5.67 1.5 1.5S10.33 8 9.5 8zm5 0c-.83 0-1.5-.67-1.5-1.5S13.67 5 14.5 5s1.5.67 1.5 1.5S15.33 8 14.5 8zm3 4c-.83 0-1.5-.67-1.5-1.5S16.67 9 17.5 9s1.5.67 1.5 1.5-.67 1.5-1.5 1.5z");

        PATHS.put(IconType.USER,
                "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z");

        PATHS.put(IconType.SAVE,
                "M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z");

        PATHS.put(IconType.DIAMOND,
                "M19 3H5L2 9l10 12L22 9l-3-6zM9.62 8l1.5-3h1.76l1.5 3H9.62zM11 10v6.68L5.44 10H11zm2 0h5.56L13 16.68V10zm3.87-2H15.1l-1.5-3h3.04l1.23 3zm-9.74 0H4.27l1.23-3h3.04l-1.5 3z");
    }

    private IconUtil() {
    }

    /**
     * Creates a vector SVG icon node.
     *
     * @param type     icon type
     * @param size     display size in pixels (e.g. 16)
     * @param colorHex hex color string (e.g. "#6366F1")
     * @return JavaFX Node containing the scalable vector icon
     */
    public static Node getIcon(IconType type, double size, String colorHex) {
        String pathData = PATHS.getOrDefault(type, PATHS.get(IconType.GIFT));
        SVGPath svg = new SVGPath();
        svg.setContent(pathData);
        svg.setFill(Color.web(colorHex));

        double scale = (size * 0.95) / 24.0;
        svg.setScaleX(scale);
        svg.setScaleY(scale);

        StackPane container = new StackPane(svg);
        container.setPrefSize(size, size);
        container.setMinSize(size, size);
        container.setMaxSize(size, size);
        container.setAlignment(Pos.CENTER);
        return container;
    }

    /**
     * Resolves an item's category to an appropriate vector category icon.
     */
    public static Node getCategoryIcon(String category, double size, String colorHex) {
        if (category == null) {
            return getIcon(IconType.GIFT, size, colorHex);
        }
        String cat = category.trim().toUpperCase();
        if (cat.contains("GPU") || cat.contains("HARDWARE")) {
            return getIcon(IconType.GPU, size, colorHex);
        } else if (cat.contains("CONSOLE") || cat.contains("GAME") || cat.contains("HANDHELD")) {
            return getIcon(IconType.GAMEPAD, size, colorHex);
        } else if (cat.contains("MONITOR") || cat.contains("DISPLAY")) {
            return getIcon(IconType.MONITOR, size, colorHex);
        } else if (cat.contains("PERIPHERAL") || cat.contains("HEADPHONE") || cat.contains("AUDIO")) {
            return getIcon(IconType.HEADPHONES, size, colorHex);
        } else if (cat.contains("STEAM") || cat.contains("CARD")) {
            return getIcon(IconType.CARD, size, colorHex);
        }
        return getIcon(IconType.GIFT, size, colorHex);
    }

    /**
     * Styles a Button with an icon and label, avoiding broken Unicode emojis.
     */
    public static void styleButton(Button button, IconType type, String text, double iconSize, String colorHex) {
        if (button == null) return;
        button.setGraphic(getIcon(type, iconSize, colorHex));
        button.setText(text);
        button.setGraphicTextGap(8);
    }

    /**
     * Styles a Label with an icon and label, avoiding broken Unicode emojis.
     */
    public static void styleLabel(javafx.scene.control.Label label, IconType type, String text, double iconSize, String colorHex) {
        if (label == null) return;
        label.setGraphic(getIcon(type, iconSize, colorHex));
        label.setText(text);
        label.setGraphicTextGap(10);
    }
}
