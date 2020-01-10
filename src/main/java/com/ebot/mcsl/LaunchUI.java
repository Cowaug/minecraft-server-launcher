package com.ebot.mcsl;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Arrays;

public class LaunchUI extends JFXTabPane {
    public LaunchUI() {
        Tab mainTab = new Tab("Home");
//        mainTab.setClosable(false);
        mainTab.setContent(new VBox(getSaveDirBox(),getVersionAndLaunch()));
        this.getTabs().add(mainTab);
    }

    private Node[] setHgrow(Node... nodes) {
        Arrays.asList(nodes).forEach(e -> HBox.setHgrow(e, Priority.ALWAYS));
        return nodes;
    }

    private HBox getSaveDirBox() {
        HBox saveDirBox = new HBox(16);

        Label saveDirLabel = new Label("Save directory");
        saveDirLabel.setPadding(new Insets(4, 0, 0, 0));

        JFXTextField dirField = new JFXTextField(Main.defaultPath);
        dirField.setEditable(false);

        JFXButton changeSaveDirBtn = new JFXButton("Change");

        saveDirBox.getChildren().addAll(setHgrow(saveDirLabel, dirField, changeSaveDirBtn));
        saveDirBox.setPadding(new Insets(16));

        return saveDirBox;
    }

    private HBox getVersionAndLaunch() {
        HBox versionLaunchBox = new HBox(16);

        Label versionLabel = new Label("Server version");
        versionLabel.setPadding(new Insets(4,0,0,0));

        JFXListView<String> serverList = new JFXListView();
        //TODO add server from folder, checking jar file and eula
        serverList.getItems().add("1.15.1");
        serverList.getItems().add("1.15.2");
        serverList.getItems().add("1.15.3");
        serverList.getSelectionModel().select(0);

        JFXButton startBtn = new JFXButton("Start");
        startBtn.setOnAction(event -> {
            Tab newTab = new Tab(serverList.getSelectionModel().getSelectedItem());
            newTab.setClosable(true);
            newTab.setContent(new ServerTab());
            LaunchUI.this.getTabs().add(newTab);
            LaunchUI.this.getSelectionModel().select(newTab);
        });

        versionLaunchBox.getChildren().addAll(setHgrow(versionLabel,serverList,startBtn));
        versionLaunchBox.setPadding(new Insets(16));

        return versionLaunchBox;
    }
}
