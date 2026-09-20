package com.dreamstop.util;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public final class UiStyleUtil {

    private UiStyleUtil() {
    }

    public static void applyAvatar(StackPane avatar, String color, String sizeClass) {
        avatar.getStyleClass().add(sizeClass);
        avatar.setBackground(new Background(
                new BackgroundFill(Color.web(color), new CornerRadii(50), Insets.EMPTY)));
    }
}