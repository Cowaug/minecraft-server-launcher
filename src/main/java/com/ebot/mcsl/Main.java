package com.ebot.mcsl;

import com.jfoenix.controls.JFXButton;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

import static com.ebot.mcsl.GUI.*;

public class Main extends Application {
//    public static final String path = System.getenv("APPDATA") + "\\.minecraft";
//    public static final String defaultPath = path + "\\minecraft server launcher";
    public static final String defaultPath = System.getProperty("user.dir") + "\\.mcsl";
    private double xOffset = 0;
    private double yOffset = 0;

    private Boolean resizebottom = false;
    private double dx;
    private double dy;

    @Override
    public void start(Stage primaryStage) {
        Parent root = new GUI(primaryStage, this);

        Scene scene = new Scene(root, 1280, 720);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setTitle("Minecraft Server Launcher");
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.DECORATED);

        primaryStage.setResizable(true);
        Platform.setImplicitExit(false);
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.initOwner(primaryStage);
            root.setEffect(new BoxBlur(4, 4, 4));

            Scene dialogScene;

            VBox mainBox = new VBox(8);
            Label exitLabel = new Label("Terminate any running server? (May cause data lost)");
            JFXButton exitBtn = new JFXButton("Terminate all and exit");
            JFXButton cancelButton = new JFXButton("Cancel");
            cancelButton.setStyle(buttonGreyStyle);
            cancelButton.setOnAction(e -> {
                dialog.close();
                root.setEffect(new BoxBlur(0, 0, 0));

            });
            exitBtn.setOnAction(e -> {
                ServerManager.terminateAllServer();
                UserConfig.writeServerLocation(ServerManager.getMinecraftServers());
                Platform.exit();
            });
            exitBtn.setMaxWidth(Double.MAX_VALUE);
            cancelButton.setMaxWidth(Double.MAX_VALUE);
            exitBtn.setStyle(buttonRedStyle);
            mainBox.setPadding(boxPadding);
            mainBox.setStyle(dialogStyle);
            mainBox.getChildren().addAll(exitLabel, exitBtn, cancelButton);
            dialogScene = new Scene(new Group(mainBox));
            dialog.setScene(dialogScene);
            dialog.show();
        });
//        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) {
        try {
            File file = new File(defaultPath);
            file.mkdir();
            UserConfig.readUserConfig();
            ServerManager.scanServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        launch(args);
    }
}
