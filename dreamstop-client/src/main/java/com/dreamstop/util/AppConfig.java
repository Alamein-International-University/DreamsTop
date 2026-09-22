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

/**
 * Single source of truth for DreamsTop application configuration,
 * brand identity metadata, default network settings, and logo generation.
 */
public final class AppConfig {

    // ==========================================
    // Brand & Application Metadata
    // ==========================================
    public static final String APP_NAME = "DreamsTop";
    public static final String APP_TAGLINE = "DreamsTop App";
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
     * Generates a modern brand logo badge.
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

        javafx.scene.control.Label letter = new javafx.scene.control.Label("D");
        letter.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: " + Math.round(size * 0.54) + "px;");

        logoBox.getChildren().add(letter);
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
