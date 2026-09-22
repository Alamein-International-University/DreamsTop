package com.dreamstop.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.SVGPath;

/**
 * Single source of truth for DreamsTop application configuration,
 * brand identity metadata, default network settings, and logo generation.
 */
public final class AppConfig {

    // ==========================================
    // Brand & Application Metadata
    // ==========================================
    public static final String APP_NAME = "DreamsTop";
    public static final String APP_TAGLINE = "i-Wish Desktop Platform";
    public static final String APP_VERSION = "1.0.0-RELEASE";
    public static final String APP_WINDOW_TITLE = APP_NAME + " — " + APP_TAGLINE;
    public static final String CURRENCY = "EGP";

    // ==========================================
    // Network Defaults
    // ==========================================
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 5005;

    // Cache for window stage icon
    private static Image cachedAppIcon;

    private AppConfig() {
    }

    /**
     * Generates a modern vector brand logo container.
     *
     * @param size size in pixels (e.g. 36)
     * @return styled JavaFX Node ready to display in headers or toolbars
     */
    public static Node createBrandLogo(double size) {
        StackPane logoBox = new StackPane();
        logoBox.setPrefSize(size, size);
        logoBox.setMinSize(size, size);
        logoBox.setMaxSize(size, size);
        logoBox.setAlignment(Pos.CENTER);

        // Gradient background with rounded squircle
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#6366F1")),
                new Stop(1, Color.web("#8B5CF6"))
        );
        logoBox.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(size * 0.28), Insets.EMPTY)));

        // Vector Gift Box icon
        SVGPath gift = new SVGPath();
        gift.setContent("M20 6h-2.18c.11-.31.18-.65.18-1 0-1.66-1.34-3-3-3-1.05 0-1.96.54-2.5 1.35l-.5.65-.5-.65C10.96 2.54 10.05 2 9 2 7.34 2 6 3.34 6 5c0 .35.07.69.18 1H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-5-2c.55 0 1 .45 1 1s-.45 1-1 1h-2v-1c0-.55.45-1 1-1zM9 4c.55 0 1 .45 1 1v1H8c0-.55.45-1 1-1zm11 15H4v-2h16v2zm0-5H4V8h5.08L7 10.83 8.62 12 11 8.76V14h2V8.76L15.38 12 17 10.83 14.92 8H20v6z");
        gift.setFill(Color.WHITE);
        double iconScale = (size * 0.58) / 24.0;
        gift.setScaleX(iconScale);
        gift.setScaleY(iconScale);

        logoBox.getChildren().add(gift);
        return logoBox;
    }

    /**
     * Generates a high-res application icon for the OS taskbar and window stage.
     */
    public static Image getAppWindowIcon() {
        if (cachedAppIcon != null) {
            return cachedAppIcon;
        }

        try {
            Node logoNode = createBrandLogo(64);
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            cachedAppIcon = logoNode.snapshot(params, null);
        } catch (Exception e) {
            // Fallback empty writable image
            cachedAppIcon = new WritableImage(32, 32);
        }
        return cachedAppIcon;
    }
}
