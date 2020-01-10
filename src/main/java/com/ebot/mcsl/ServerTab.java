package com.ebot.mcsl;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import javax.swing.border.EmptyBorder;

public class ServerTab extends VBox {
    public ServerTab() {
        final ServerConsole serverConsole = new ServerConsole();






        Label label = new Label("Label");
        final TextArea textArea = new TextArea();
        textArea.setEditable(false);
        final Button button = new Button("start");
        Button button2 = new Button("stop");
        Button forceStop = new Button("FStop");
        button.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                serverConsole.run("java -Xmx16384M -Xms2048M -XX:+UseG1GC -jar forge-1.14.4-28.0.45.jar nogui TRUE", textArea);
                button.setDisable(true);
            }
        });
        button2.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                serverConsole.stop();
                button.setDisable(false);
            }
        });
        forceStop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                serverConsole.fstop();
                button.setDisable(false);
            }
        });
        this.getChildren().add(label);
        this.getChildren().add(textArea);
        this.getChildren().add(button);
        this.getChildren().add(button2);
        this.getChildren().add(forceStop);
    }
}