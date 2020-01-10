package com.ebot.mcsl;

import com.jfoenix.controls.*;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LaunchUI extends JFXTabPane {
    public LaunchUI(){
        Tab mainTab = new Tab("Home");
        mainTab.setClosable(false);

        HBox saveDir = new HBox(2);

        JFXTextField dirField = new JFXTextField(Main.defaultPath);
        dirField.setEditable(false);
        JFXButton changeDirBtn = new JFXButton("Change");

        saveDir.getChildren().addAll(dirField,changeDirBtn);

        Button button = new Button("button1");
        button.setOnAction(event -> {
            Tab newTab = new Tab("abc");
            newTab.setContent(new ServerTab());
            LaunchUI.this.getTabs().add(newTab);
        });

        mainTab.setContent(saveDir);
        this.getTabs().add(mainTab);
    }
}
