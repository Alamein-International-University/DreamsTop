package com.dreamstop.util;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;

/**
 * Manages active application theme (Dark Glassmorphism vs. Clean Light),
 * persists user preference, and dynamically updates JavaFX scenes and roots.
 */
public final class ThemeManager {

    public enum Theme {
        DARK("theme-dark", "Dark Glassmorphism"),
        LIGHT("theme-light", "Clean Light");

        private final String styleClass;
        private final String displayName;

        Theme(String styleClass, String displayName) {
            this.styleClass = styleClass;
            this.displayName = displayName;
        }

        public String getStyleClass() {
            return styleClass;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public interface ThemeChangeListener {
        void onThemeChanged(Theme newTheme);
    }

    private static final String PREF_KEY_THEME = "dreamstop_app_theme";
    private static final Preferences PREFS = Preferences.userNodeForPackage(ThemeManager.class);
    private static final List<ThemeChangeListener> LISTENERS = new CopyOnWriteArrayList<>();

    private static Theme currentTheme;
    private static Scene activeScene;

    static {
        String saved = PREFS.get(PREF_KEY_THEME, Theme.DARK.name());
        try {
            currentTheme = Theme.valueOf(saved);
        } catch (Exception e) {
            currentTheme = Theme.DARK;
        }
    }

    private ThemeManager() {
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    public static boolean isDarkMode() {
        return currentTheme == Theme.DARK;
    }

    public static void registerScene(Scene scene) {
        activeScene = scene;
        applyTheme(scene);
    }

    public static void setTheme(Theme theme) {
        if (theme == null || theme == currentTheme) {
            return;
        }
        currentTheme = theme;
        try {
            PREFS.put(PREF_KEY_THEME, theme.name());
        } catch (Exception ignored) {
        }

        if (activeScene != null) {
            applyTheme(activeScene);
        }

        notifyListeners(theme);
    }

    public static void toggleTheme() {
        setTheme(currentTheme == Theme.DARK ? Theme.LIGHT : Theme.DARK);
    }

    public static void applyTheme(Scene scene) {
        if (scene == null) return;
        applyTheme(scene.getRoot());
    }

    public static void applyTheme(Parent root) {
        if (root == null) return;
        Runnable action = () -> {
            root.getStyleClass().removeAll(Theme.DARK.getStyleClass(), Theme.LIGHT.getStyleClass());
            root.getStyleClass().add(currentTheme.getStyleClass());
        };
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    /**
     * Styles any Dialog/Alert to strictly follow the active theme (Dark Glassmorphism or Clean Light).
     */
    public static void styleDialog(Dialog<?> dialog) {
        if (dialog == null) return;
        DialogPane pane = dialog.getDialogPane();
        if (pane == null) return;

        String css = ThemeManager.class.getResource("/com/dreamstop/css/styles.css").toExternalForm();
        if (!pane.getStylesheets().contains(css)) {
            pane.getStylesheets().add(css);
        }

        if (!pane.getStyleClass().contains("modern-dialog-pane")) {
            pane.getStyleClass().add("modern-dialog-pane");
        }
        applyTheme(pane);

        dialog.setOnShown(e -> {
            try {
                Stage stage = (Stage) pane.getScene().getWindow();
                stage.getIcons().setAll(AppConfig.getAppWindowIcon());
            } catch (Exception ignored) {
            }
        });
    }

    public static void addListener(ThemeChangeListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void removeListener(ThemeChangeListener listener) {
        LISTENERS.remove(listener);
    }

    private static void notifyListeners(Theme newTheme) {
        Platform.runLater(() -> {
            for (ThemeChangeListener listener : LISTENERS) {
                try {
                    listener.onThemeChanged(newTheme);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
