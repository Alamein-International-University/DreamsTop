package com.dreamstop.server.gui;

import com.dreamstop.server.ServerDaemon;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerGuiApp extends Application {

    private ServerDaemon daemon;

    @Override
    public void start(Stage stage) throws Exception {
        daemon = new ServerDaemon();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dreamstop/server/gui/server_dashboard.fxml"));
        Parent root = loader.load();

        ServerGuiController controller = loader.getController();
        controller.setDaemon(daemon);

        Scene scene = new Scene(root, 820, 620);
        stage.setTitle("DreamsTop Server Controller 🛡️");
        stage.setMinWidth(780);
        stage.setMinHeight(580);
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            if (daemon != null && daemon.isRunning()) {
                daemon.stop();
            }
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
