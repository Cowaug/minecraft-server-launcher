package com.ebot.mcsl;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {
    public static final String path = System.getenv("APPDATA") + "\\.minecraft";
    public static final String defaultPath = path + "\\minecraft server launcher";

    @Override
    public void start(Stage primaryStage) {
        Parent root = new GUI(primaryStage,this);

        primaryStage.setTitle("Minecraft Server Launcher");
        primaryStage.setScene(new Scene(root, 1280, 720));
//        primaryStage.setResizable(false);
//        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            UserConfig.writeServerLocation(ServerManager.getMinecraftServers());
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) {
        try {
            File file = new File(defaultPath);
            file.mkdir();
            UserConfig.readUserConfig();
            ServerManager.scanServer(UserConfig.getUserPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        launch(args);
    }
}
