package com.ebot.mcsl;

import com.jfoenix.controls.JFXButton;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
    public static final String defaultPath = System.getenv("APPDATA") + "\\.minecraft";

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = new LaunchUI();

        primaryStage.setTitle("Minecraft Server Launcher");
        primaryStage.setScene(new Scene(root, 1280, 720));
//        primaryStage.setResizable(false);
//        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
