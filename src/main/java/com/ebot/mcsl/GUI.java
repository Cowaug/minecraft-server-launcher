package com.ebot.mcsl;

import com.jfoenix.controls.*;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;

public class GUI extends JFXTabPane {
    private Stage stage;
    private final String boxStyle = "-fx-border-color: #00000020;\n" +
            "-fx-border-insets: 0;\n" +
            "-fx-border-width: 2;\n";
    private final String buttonStyle =
            "    -fx-background-color: rgb(77,102,204);\n" +
                    "    -fx-text-fill: WHITE;";

    private final Insets boxPadding = new Insets(8);
    private final Insets labelPadding = new Insets(4, 0, 0, 0);

    public GUI(Stage stage) {
        this.stage = stage;
        Tab mainTab = new Tab("Home");


        mainTab.setContent(new MainTab());
        this.getTabs().add(mainTab);
    }


    class ServerTab extends VBox {
        Label label = new Label("Label");
        final TextArea textArea = new TextArea();
        final Button button = new Button("start");
        Button button2 = new Button("stop");
        Button forceStop = new Button("FStop");
        Button closeBtn = new Button("Exit Server");

        ServerTab(MinecraftServer minecraftServer, Tab tab) {
            textArea.setEditable(false);
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
                GUI.this.getTabs().remove(tab);
            });


            this.getChildren().add(label);
            this.getChildren().add(textArea);
            this.getChildren().add(button);
            this.getChildren().add(button2);
            this.getChildren().add(forceStop);
            this.getChildren().add(closeBtn);
        }
    }

    class MainTab extends VBox {
//        private HBox saveDirBox = new HBox(8);
//        private Label saveDirLabel = new Label("Save directory");
//        private JFXTextField saveDirField = new JFXTextField(UserConfig.getUserPath());
//        private JFXButton changeSaveDirBtn = new JFXButton("Change");
//
//        // select / add server
//        private HBox versionLaunchBox = new HBox(8);
//        private Label versionLabel = new Label("Select sever");
//        private JFXComboBox<String> serverList = new JFXComboBox<>();
//        private JFXButton startBtn = new JFXButton("Launch");

        private MinecraftServer currentServer;

        private HBox topBox = new HBox(8);

        private HBox selectServerBox = new HBox(8);
        private Label selectServerLabel = new Label("Select server");
        private JFXComboBox<String> serverList = new JFXComboBox<>();
        private JFXButton openServerLocationBtn = new JFXButton("Open server location");
        private Label serverPathLabel = new Label("Path: ");
        private JFXTextField serverPath = new JFXTextField();

        private HBox addServerBox = new HBox(8);
        private JFXButton addNewServerBtn = new JFXButton("Add new server...");
        private JFXButton addExistServerBtn = new JFXButton("Add exist server...");

        private VBox bottomBox = new VBox(8);
        private HBox serverProperties = new HBox(8);
        private Label jarFileLabel = new Label("Execute jar file");
        private JFXComboBox<String> jarFileSelect = new JFXComboBox<>();
        private Label ramLabel = new Label("Server memory");
        private JFXComboBox<String> serverRamList = new JFXComboBox<>();
        private JFXButton startServerBtn = new JFXButton("Start server");
        private JFXButton renameServerBtn = new JFXButton("Rename server");
        private JFXButton deleteButtonBtn = new JFXButton("Delete server");
        private JFXTreeTableView configTable = new JFXTreeTableView();


        MainTab() {
            //region Top box
            selectServerLabel.setPadding(labelPadding);
            serverList.getItems().addAll(ServerManager.getServerList());
            serverList.setOnAction(event -> {
                currentServer = ServerManager.getMinecraftServer(serverList.getSelectionModel().getSelectedItem());
                serverPath.setText(currentServer.getServerLocation());
                openServerLocationBtn.setDisable(false);
                loadConfig();
            });
            serverPath.setEditable(false);
            serverPathLabel.setPadding(labelPadding);
            openServerLocationBtn.setDisable(true);
            openServerLocationBtn.setStyle(buttonStyle);
            openServerLocationBtn.setOnAction(event -> {
                try {
                    Runtime.getRuntime().exec("explorer.exe \\select," + currentServer.getServerLocation());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            selectServerBox.setStyle(boxStyle);
            selectServerBox.setPadding(boxPadding);
            selectServerBox.getChildren().addAll(setHgrow(selectServerLabel, serverList, openServerLocationBtn, serverPathLabel, serverPath));

            addNewServerBtn.setStyle(buttonStyle);
            addExistServerBtn.setStyle(buttonStyle);
            addServerBox.setStyle(boxStyle);
            addServerBox.setPadding(boxPadding);
            addServerBox.getChildren().addAll(addNewServerBtn, addExistServerBtn);
            HBox.setHgrow(selectServerBox, Priority.ALWAYS);
            topBox.getChildren().addAll(selectServerBox, addServerBox);
            //endregion

            //region Bottom Box
            jarFileLabel.setPadding(labelPadding);
            ramLabel.setPadding(labelPadding);
            for (float i = 0; i <= 16; i = i + 0.25f) {
                serverRamList.getItems().add((int) (i * 1024) + " MB (" + i + " GB)");
            }
            serverRamList.getSelectionModel().select(1);
            startServerBtn.setOnAction(event -> {
                currentServer.saveConfig();
            });
            renameServerBtn.setOnAction(event -> {

            });
            deleteButtonBtn.setOnAction(event -> {

            });

            JFXTreeTableColumn<MinecraftServer.Config, String> attrCol = new JFXTreeTableColumn<>("Attribute");

            attrCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<MinecraftServer.Config, String> param) -> {
                if (attrCol.validateValue(param)) return param.getValue().getValue().getAttributeProp();
                else return attrCol.getComputedValue(param);
            });
            attrCol.prefWidthProperty().bind(configTable.widthProperty().divide(2));
            attrCol.setEditable(false);

            JFXTreeTableColumn<MinecraftServer.Config, String> valueCol = new JFXTreeTableColumn<>("Value");
            valueCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<MinecraftServer.Config, String> param) -> {
                if (valueCol.validateValue(param)) return param.getValue().getValue().getValueProp();
                else return valueCol.getComputedValue(param);
            });
            valueCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
            valueCol.setOnEditCommit((TreeTableColumn.CellEditEvent<MinecraftServer.Config, String> t) -> {
                String s = "";
                try {
                    t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().setValue(t.getNewValue());
                    return;
//                } catch (NumberFormatException e) {
//                    e.printStackTrace();
//                    s = "Expect numbers.";
                } catch (Exception e) {
//                    e.printStackTrace();
//                    s = e.getMessage();
                    configTable.refresh();
                }
//                final Stage dialog = new Stage();
//                dialog.initModality(Modality.APPLICATION_MODAL);
//                dialog.initOwner(stage);
//                Scene dialogScene = new Scene(new Label(s),480,320);
//                dialog.setScene(dialogScene);
//                dialog.show();
//                configTable.refresh();
            });
            valueCol.prefWidthProperty().bind(configTable.widthProperty().divide(2));
            valueCol.setEditable(true);

            configTable.getColumns().addAll(attrCol, valueCol);
            configTable.getColumns().addListener(new ListChangeListener() {
                public boolean suspended;

                @Override
                public void onChanged(Change change) {
                    change.next();
                    if (change.wasReplaced() && !suspended) {
                        this.suspended = true;
                        configTable.getColumns().setAll(attrCol, valueCol);
                        this.suspended = false;
                    }
                }
            });


            configTable.setEditable(true);
            configTable.setShowRoot(false);

            jarFileSelect.setMaxWidth(Double.MAX_VALUE);
            serverProperties.getChildren().addAll(setHgrow(jarFileLabel, jarFileSelect, ramLabel, serverRamList, startServerBtn, renameServerBtn, deleteButtonBtn));

            bottomBox.setPadding(boxPadding);
            bottomBox.setStyle(boxStyle);
            bottomBox.getChildren().addAll(serverProperties, configTable);
            //endregion
            this.getChildren().addAll(topBox, bottomBox);
            this.setPadding(new Insets(8));
            this.setSpacing(8);
        }

        private void loadConfig(){
            TreeItem<MinecraftServer.Config> root = new TreeItem<>(new MinecraftServer.Config("abc", "1"));
            currentServer.getConfigs().forEach(e->{
                root.getChildren().add(new TreeItem<>(e));
            });
            configTable.setRoot(root);
        }
    }

    private void popupAddServer() {
        VBox vBox = new VBox();


        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        Scene dialogScene = new Scene(vBox);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private Node[] setHgrow(Node... nodes) {
        Arrays.asList(nodes).forEach(e -> HBox.setHgrow(e, Priority.ALWAYS));
        return nodes;
    }
}

