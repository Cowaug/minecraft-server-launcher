package com.ebot.mcsl;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static final String defaultPath = System.getenv("APPDATA") + "\\.minecraft";

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = new LaunchUI();
        primaryStage.setTitle("Minecraft Server Launcher");
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
