package com.dreamstop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App - DreamsTop (i-Wish)
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/dreamstop/view/login_view.fxml"));
        scene = new Scene(root, 1100, 720);
        stage.setTitle("DreamsTop (i-Wish) 🎁");
        stage.setMinWidth(1050);
        stage.setMinHeight(640);
        stage.setScene(scene);

        stage.setOnCloseRequest(e -> {
            new Thread(() -> {
                com.dreamstop.network.NetworkClient.getInstance().disconnect();
            }, "client-exit").start();
            javafx.application.Platform.exit();
            System.exit(0);
        });

        stage.show();
    }

    @Override
    public void stop() throws Exception {
        new Thread(() -> {
            com.dreamstop.network.NetworkClient.getInstance().disconnect();
        }, "client-exit").start();
        super.stop();
        System.exit(0);
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/dreamstop/view/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}