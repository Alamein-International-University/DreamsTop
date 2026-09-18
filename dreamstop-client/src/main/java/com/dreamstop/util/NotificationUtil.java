package com.dreamstop.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class NotificationUtil {

    private static StackPane globalToastContainer;

    public static void registerToastContainer(StackPane container) {
        globalToastContainer = container;
    }

    public static void showSuccess(String message) {
        showToast(message, "#10B981", "✓ ");
    }

    public static void showError(String message) {
        showToast(message, "#EF4444", "✕ ");
    }

    public static void showInfo(String message) {
        showToast(message, "#6366F1", "ℹ ");
    }

    public static void showWarning(String message) {
        showToast(message, "#F59E0B", "⚠ ");
    }

    private static void showToast(String message, String bgColor, String prefix) {
        if (globalToastContainer == null) return;

        Label label = new Label(prefix + message);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

        HBox toast = new HBox(label);
        toast.setAlignment(Pos.CENTER);
        toast.setMaxWidth(380);
        toast.setMinHeight(42);
        toast.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 20px; -fx-padding: 10px 20px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 10, 0, 0, 4);",
                bgColor
        ));
        toast.setOpacity(0);

        StackPane.setAlignment(toast, Pos.TOP_CENTER);
        StackPane.setMargin(toast, new javafx.geometry.Insets(20, 0, 0, 0));

        globalToastContainer.getChildren().add(toast);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), toast);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        PauseTransition stay = new PauseTransition(Duration.millis(2800));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(350), toast);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        SequentialTransition seq = new SequentialTransition(fadeIn, stay, fadeOut);
        seq.setOnFinished(e -> globalToastContainer.getChildren().remove(toast));
        seq.play();
    }
}
