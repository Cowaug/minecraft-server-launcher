package com.ebot.mcsl.GUI;

import com.ebot.mcsl.MinecraftServer;
import com.ebot.mcsl.ServerManager;
import com.ebot.mcsl.UserConfig;
import com.jfoenix.controls.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;

@SuppressWarnings("FieldCanBeLocal")
public class LaunchUI extends JFXTabPane {
    Stage stage;

    /**
     * UI COMPONENTS
     */
    // save directory
    private HBox saveDirBox = new HBox(16);
    private Label saveDirLabel = new Label("Save directory");
    private JFXTextField saveDirField = new JFXTextField(UserConfig.getUserPath());
    private JFXButton changeSaveDirBtn = new JFXButton("Change");

    // select / add server
    private HBox versionLaunchBox = new HBox(16);
    private Label versionLabel = new Label("Select sever");
    private JFXComboBox<String> serverList = new JFXComboBox<>();
    private JFXButton startBtn = new JFXButton("Launch");

    public LaunchUI(Stage stage) {
        this.stage = stage;
        Tab mainTab = new Tab("Home");

        //region Save Directory
        saveDirLabel.setPadding(new Insets(4, 0, 0, 0));
        saveDirField.setEditable(false);
        changeSaveDirBtn.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(stage);
            saveDirField.setText(selectedDirectory.getAbsolutePath());
            UserConfig.writeUserConfig(saveDirField.getText());
            ServerManager.scanServer(UserConfig.getUserPath());
            serverList.getItems().clear();
            serverList.getItems().addAll(ServerManager.getServerList());
            serverList.getSelectionModel().select(0);
        });
        saveDirBox.getChildren().addAll(setHgrow(saveDirLabel, saveDirField, changeSaveDirBtn));
        saveDirBox.setPadding(new Insets(16));
        //endregion

        //region Server List
        versionLabel.setPadding(new Insets(4, 0, 0, 0));
        //TODO add server from folder, checking jar file and eula
        serverList.getItems().addAll(ServerManager.getServerList());
        serverList.getItems().add(0, "Add server...");
        if (serverList.getItems().size() == 1)
            serverList.getSelectionModel().select(0);
        else
            serverList.getSelectionModel().select(1);
        serverList.setOnAction(event -> {
            if (serverList.getSelectionModel().getSelectedIndex() == 0) {
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(stage);
                Scene dialogScene = new Scene(new AddServerWindow());
                dialog.setScene(dialogScene);
                dialog.show();

                // TODO: open add new server
                serverList.getSelectionModel().clearSelection();
            }
        });
        startBtn.setOnAction(event -> {
            if (serverList.getSelectionModel().getSelectedIndex() == 0) {
                // TODO: open add new server
                return;
            }
            Tab newTab = new Tab(serverList.getSelectionModel().getSelectedItem());
            newTab.setClosable(true);
            newTab.setContent(new ServerTab(ServerManager.getMinecraftServer(newTab.getText()), newTab));
            LaunchUI.this.getTabs().add(newTab);
            LaunchUI.this.getSelectionModel().select(newTab);
        });
        versionLaunchBox.getChildren().addAll(setHgrow(versionLabel, serverList, startBtn));
        versionLaunchBox.setPadding(new Insets(16));
        //endregion

        mainTab.setContent(new VBox(saveDirBox, versionLaunchBox));
        this.getTabs().add(mainTab);
    }

    private Node[] setHgrow(Node... nodes) {
        Arrays.asList(nodes).forEach(e -> HBox.setHgrow(e, Priority.ALWAYS));
        return nodes;
    }

    class ServerTab extends VBox {
        ServerTab(MinecraftServer minecraftServer, Tab tab) {
            Label label = new Label("Label");
            final TextArea textArea = new TextArea();
            textArea.setEditable(false);
            final Button button = new Button("start");
            Button button2 = new Button("stop");
            Button forceStop = new Button("FStop");
            Button closeBtn = new Button("Exit Server");

            button.setOnAction(event -> {
                minecraftServer.startServer(textArea);
                button.setDisable(true);
            });
            button2.setOnAction(event -> {
                minecraftServer.saveAndStop(textArea);
                button.setDisable(false);
            });
            forceStop.setOnAction(event -> {
                minecraftServer.forceStop(textArea);
                button.setDisable(false);
            });
            closeBtn.setOnAction(event -> {
                LaunchUI.this.getTabs().remove(tab);
            });


            this.getChildren().add(label);
            this.getChildren().add(textArea);
            this.getChildren().add(button);
            this.getChildren().add(button2);
            this.getChildren().add(forceStop);
            this.getChildren().add(closeBtn);
        }
    }

    class AddServerWindow extends VBox {
        AddServerWindow() {
            JFXComboBox<String> comboBox = new JFXComboBox<>();
            this.getChildren().add(comboBox);
        }
    }
}

