package com.ebot.mcsl;

import com.jfoenix.controls.*;
import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class GUI extends JFXTabPane {
    private Stage stage;
    private Application application;
    public static final String boxStyle = "-fx-border-color: #00000020;\n" +

            "-fx-border-insets: 0;\n" +
            "-fx-border-width: 2;\n";
    public static final String dialogStyle = "-fx-border-color: #00BCD4;\n" +
            "-fx-border-corner: 8 8 8 8;\n" +
            "-fx-border-width: 2;\n";
    public static final String buttonStyle =
            "    -fx-background-color: rgb(77,102,204);\n" +
                    "-fx-focus-color: transparent;\n" +
                    "-fx-faint-focus-color: transparent;\n" +
                    "    -fx-text-fill: WHITE;";
    public static final String buttonRedStyle =
            "    -fx-background-color: #C75450;\n" +
                    "-fx-focus-color: transparent;\n" +
                    "-fx-faint-focus-color: transparent;\n" +
                    "    -fx-text-fill: WHITE;";

    public static final Insets boxPadding = new Insets(8);
    public static final Insets labelPadding = new Insets(4, 0, 0, 0);

    public GUI(Stage stage, Application application) {
        this.stage = stage;
        this.application = application;
        Tab mainTab = new Tab("Home");

        mainTab.setContent(new MainTab());

        this.getTabs().add(mainTab);
    }

    public void reload() {
        Tab mainTab = new Tab("Home");

        mainTab.setContent(new MainTab());
        this.getTabs().clear();
        this.getTabs().add(mainTab);
    }

    class ServerTab extends VBox {
        ServerTab(MinecraftServer minecraftServer, Tab tab) {

        }
    }

    class MainTab extends VBox {
        private MinecraftServer currentServer;

        MainTab() {
            //region Variables
            HBox topBox = new HBox(8);

            HBox selectServerBox = new HBox(8);
            Label selectServerLabel = new Label("Select server");
            JFXComboBox<String> serverList = new JFXComboBox<>();
            JFXButton openServerLocationBtn = new JFXButton("Open server location");
            Label serverPathLabel = new Label("Path: ");
            JFXTextField serverPath = new JFXTextField();

            HBox addServerBox = new HBox(8);
            JFXButton addNewServerBtn = new JFXButton("Add new server...");
            JFXButton addExistServerBtn = new JFXButton("Add exist server...");

            VBox bottomBox = new VBox(8);
            HBox serverProperties = new HBox(8);
            Label jarFileLabel = new Label("Execute jar file");
            JFXComboBox<String> jarFileSelect = new JFXComboBox<>();
            Label ramLabel = new Label("Server memory");
            JFXComboBox<String> serverRamList = new JFXComboBox<>();
            JFXButton startServerBtn = new JFXButton("Start server");
            JFXButton renameServerBtn = new JFXButton("Rename server");
            JFXButton deleteButtonBtn = new JFXButton("Delete server");
            JFXTreeTableView<MinecraftServer.Config> configTable = new JFXTreeTableView<>();

            HBox labelBox = new HBox(8);
            Label warningLabel = new Label("");
            Hyperlink helpLabel = new Hyperlink("What are these values mean?");
            //endregion

            //region Styles
            GUI.this.setPadding(labelPadding, selectServerLabel, serverPathLabel, jarFileLabel, warningLabel, ramLabel);
            GUI.this.setPadding(boxPadding, topBox, selectServerBox, addServerBox, serverProperties, labelBox);
            GUI.this.setPadding(boxPadding, bottomBox);
            GUI.this.setStyle(boxStyle, selectServerBox, addServerBox, bottomBox);
            GUI.this.setStyle(buttonStyle, openServerLocationBtn, addExistServerBtn, addNewServerBtn, renameServerBtn, startServerBtn);
            GUI.this.setStyle(buttonRedStyle, deleteButtonBtn);
            GUI.this.setVGrow(configTable, bottomBox);
            GUI.this.setHGrow(selectServerLabel, serverList, openServerLocationBtn, serverPathLabel, serverPath, selectServerBox,
                    jarFileLabel, jarFileSelect, ramLabel, serverRamList, startServerBtn, renameServerBtn, deleteButtonBtn,
                    warningLabel, helpLabel);
            GUI.this.setEditable(false, serverPath);
            GUI.this.setDisable(true, bottomBox,openServerLocationBtn);
            //endregion

            //region Top box
            serverList.getItems().addAll(ServerManager.getServerList());
            serverList.setOnAction(event -> {
                try {
                    currentServer = ServerManager.getMinecraftServer(serverList.getSelectionModel().getSelectedItem());
                } catch (Exception e) {
                    e.getMessage();
                }
                serverPath.setText(currentServer.getServerLocation());
                openServerLocationBtn.setDisable(false);
                bottomBox.setDisable(false);
                configTable.setRoot(loadConfig(currentServer));
                jarFileSelect.getItems().clear();
                jarFileSelect.getItems().addAll(currentServer.getJarFileList());
                jarFileSelect.getSelectionModel().select(currentServer.getServerFileName());
                serverRamList.getSelectionModel().select(currentServer.getMaxRam() + " MB (" + (float) (currentServer.getMaxRam() / 1024.0) + " GB)");
                if (currentServer.isLaunched()) {
                    warningLabel.setText("Server is opened. Change will not affected unless you reboot the server");
                } else {
                    warningLabel.setText("");
                }
            });
            openServerLocationBtn.setOnAction(event -> {
                try {
                    Runtime.getRuntime().exec("explorer.exe \\select," + currentServer.getServerLocation());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            addNewServerBtn.setOnAction(event -> {

            });
            addExistServerBtn.setOnAction(event -> {
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initStyle(StageStyle.UNDECORATED);
                dialog.initOwner(stage);
                GUI.this.setEffect(new BoxBlur(4, 4, 4));

                Scene dialogScene;

                VBox mainBox = new VBox(8);
                //reuse Path label
                Label nameLabel = new Label("Server name");
                nameLabel.setPadding(labelPadding);
                JFXTextField pathField = new JFXTextField();
                JFXCheckBox sameAsFolder = new JFXCheckBox("Same as folder");
                sameAsFolder.setDisable(true);
                AtomicReference<String> oldName = new AtomicReference<>();
                JFXButton changeBtn = new JFXButton("Select folder");
                JFXTextField nameField = new JFXTextField("Server");
                //reuse Jar label
                JFXComboBox<String> jarList = new JFXComboBox<>();
                JFXButton confirmBtn = new JFXButton("Add");
                JFXButton cancelButton = new JFXButton("Cancel");


                cancelButton.setOnAction(e -> {
                    dialog.close();
                    GUI.this.setEffect(new BoxBlur(0, 0, 0));

                });
                sameAsFolder.setOnAction(e->{
                    if(sameAsFolder.isSelected()){
                        oldName.set(nameField.getText());
                        nameField.setText(pathField.getText().substring(pathField.getText().lastIndexOf("\\")+1));
                    }else {
                        nameField.setText(oldName.get());
                    }
                    confirmBtn.setDisable(ServerManager.isDuplicate(nameField.getText()));
                });
                nameField.setOnKeyReleased(e -> {
                    if (ServerManager.isDuplicate(nameField.getText()) || nameField.getText().equals("")) {
                        confirmBtn.setDisable(true);
                    } else {
                        confirmBtn.setDisable(false);
                    }
                });
                changeBtn.setOnAction(e -> {
                    File selectedDirectory;
                    try{
                        DirectoryChooser directoryChooser = new DirectoryChooser();
                        directoryChooser.setInitialDirectory(new File(pathField.getText()));
                        selectedDirectory = directoryChooser.showDialog(stage);
                    }catch (Exception ex){
                        selectedDirectory = new DirectoryChooser().showDialog(stage);
                    }

                    if (selectedDirectory != null) {
                        sameAsFolder.setDisable(false);
                        pathField.setText(selectedDirectory.getAbsolutePath());
                        jarList.getItems().clear();
                        File[] file = new File(pathField.getText()).listFiles(f -> f.isFile() && f.getName().endsWith(".jar"));
                        if (file == null || file.length == 0) {
                            confirmBtn.setDisable(true);
                            return;
                        }
                        Arrays.asList(file).forEach(fileJar -> jarList.getItems().add(fileJar.getName()));
                        if(jarList.getItems().contains("server.jar")){
                            jarList.getSelectionModel().select("server.jar");
                        }else {
                            jarList.getSelectionModel().selectFirst();
                        }
                        confirmBtn.setDisable(ServerManager.isDuplicate(nameField.getText()));
                    }
                });
                confirmBtn.setDisable(true);
                confirmBtn.setOnAction(e -> {
                    ServerManager.addExistServer(nameField.getText(),pathField.getText(),jarList.getSelectionModel().getSelectedItem());
                    serverList.getItems().clear();
                    serverList.getItems().addAll(ServerManager.getServerList());
                    serverList.getSelectionModel().select(nameField.getText());
                    dialog.close();
                    UserConfig.writeServerLocation(ServerManager.getMinecraftServers());
                    GUI.this.setEffect(new BoxBlur(0, 0, 0));
                });
                confirmBtn.setMaxWidth(Double.MAX_VALUE);
                cancelButton.setMaxWidth(Double.MAX_VALUE);
                changeBtn.setStyle(buttonStyle);
                confirmBtn.setStyle(buttonStyle);
                cancelButton.setStyle(buttonStyle);

                HBox nameBox = new HBox(8);
                nameBox.getChildren().addAll(setHGrow(nameLabel,nameField,sameAsFolder));

                HBox pathBox = new HBox(8);
                pathBox.getChildren().addAll(setHGrow(serverPathLabel, pathField, changeBtn));

                HBox jarBox = new HBox(8);
                jarBox.getChildren().addAll(setHGrow(jarFileLabel, jarList));
                jarList.setMaxWidth(Double.MAX_VALUE);

                mainBox.setPadding(boxPadding);
                mainBox.getChildren().addAll(nameBox, pathBox, jarBox, confirmBtn, cancelButton);
                mainBox.setStyle(dialogStyle);
                mainBox.setPrefWidth(GUI.this.getWidth()/2);
                dialogScene = new Scene(new Group(mainBox));
                dialog.setScene(dialogScene);
                dialog.show();
            });
            selectServerBox.getChildren().addAll(selectServerLabel, serverList, openServerLocationBtn, serverPathLabel, serverPath);
            addServerBox.getChildren().addAll(addNewServerBtn, addExistServerBtn);
            topBox.getChildren().addAll(selectServerBox, addServerBox);
            //endregion

            //region Bottom Box
            for (float i = 0; i <= 16; i = i + 0.25f) {
                serverRamList.getItems().add((int) (i * 1024) + " MB (" + i + " GB)");
            }
            serverRamList.getSelectionModel().select(1);
            serverRamList.setOnAction(event -> {
                currentServer.setMaxRam(Integer.parseInt(serverRamList.getSelectionModel().getSelectedItem().split(" ")[0]));
            });
            startServerBtn.setOnAction(event -> {
                if (currentServer.isLaunched()) {
                    GUI.this.getSelectionModel().select(currentServer.getLaunchedTab());
                    return;
                }
                Tab newTab = new Tab(currentServer.getServerName());
                newTab.setClosable(true);
                newTab.setContent(new ServerTab(currentServer, newTab));
                currentServer.setLaunchedTab(newTab);
                GUI.this.getTabs().add(newTab);
                GUI.this.getSelectionModel().select(newTab);
                warningLabel.setText("Server is opened. Change will not affected unless you reboot the server");
            });
            renameServerBtn.setOnAction(event -> {
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initStyle(StageStyle.UNDECORATED);
                dialog.initOwner(stage);
                GUI.this.setEffect(new BoxBlur(4, 4, 4));

                Scene dialogScene;

                VBox mainBox = new VBox(8);
                JFXTextField nameField = new JFXTextField(currentServer.getServerName());
                JFXCheckBox applyToFolder = new JFXCheckBox("Rename folder to match server name");
                JFXButton confirmBtn = new JFXButton("Rename");
                JFXButton cancelButton = new JFXButton("Cancel");
                applyToFolder.setOnAction(e -> {
                    if (applyToFolder.isSelected())
                        confirmBtn.setDisable(false);
                });
                cancelButton.setOnAction(e -> {
                    dialog.close();
                    GUI.this.setEffect(new BoxBlur(0, 0, 0));

                });
                nameField.setOnKeyReleased(e -> {
                    if (ServerManager.isDuplicate(nameField.getText()) || nameField.getText().equals("")) {
                        confirmBtn.setDisable(true);
                    } else {
                        confirmBtn.setDisable(false);
                    }
                });
                applyToFolder.setSelected(false);
                confirmBtn.setDisable(true);
                confirmBtn.setOnAction(e -> {
                    String oldName = currentServer.getServerName();
                    currentServer.setServerName(nameField.getText());
                    if (applyToFolder.isSelected())
                        currentServer.renameServerLocation(nameField.getText());

                    serverList.getItems().remove(oldName);
                    serverList.getItems().add(nameField.getText());
                    serverList.getSelectionModel().select(nameField.getText());
                    serverPath.setText(currentServer.getServerLocation());
                    dialog.close();
                    UserConfig.writeServerLocation(ServerManager.getMinecraftServers());
                    GUI.this.setEffect(new BoxBlur(0, 0, 0));
                });
                confirmBtn.setMaxWidth(Double.MAX_VALUE);
                cancelButton.setMaxWidth(Double.MAX_VALUE);
                confirmBtn.setStyle(buttonStyle);
                cancelButton.setStyle(buttonStyle);
                mainBox.setPadding(boxPadding);
                mainBox.getChildren().addAll(nameField, applyToFolder, confirmBtn, cancelButton);
                mainBox.setStyle(dialogStyle);
                dialogScene = new Scene(new Group(mainBox));
                dialog.setScene(dialogScene);
                dialog.show();
            });
            deleteButtonBtn.setOnAction(event -> {
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initStyle(StageStyle.UNDECORATED);
                dialog.initOwner(stage);
                GUI.this.setEffect(new BoxBlur(4, 4, 4));
                Scene dialogScene;

                VBox mainBox = new VBox(8);
                Label deleteLabel = new Label("You will have to manually remove file from disk");
                JFXButton deleteServerBtn = new JFXButton("Delete server");
                JFXButton deleteServerOpenRootBtn = new JFXButton("Delete server and open folder");
                JFXButton cancelButton = new JFXButton("Cancel");
                cancelButton.setOnAction(e -> {
                    dialog.close();
                    GUI.this.setEffect(new BoxBlur(0, 0, 0));

                });

                deleteServerBtn.setOnAction(e -> {
                    ServerManager.removeMinecraftServer(currentServer);
                    reload();
                    dialog.close();
                    UserConfig.writeServerLocation(ServerManager.getMinecraftServers());
                    GUI.this.setEffect(new BoxBlur(0, 0, 0));

                });

                deleteServerOpenRootBtn.setOnAction(e -> {
                    openServerLocationBtn.fire();
                    ServerManager.removeMinecraftServer(currentServer);
                    reload();
                    dialog.close();
                    UserConfig.writeServerLocation(ServerManager.getMinecraftServers());
                    GUI.this.setEffect(new BoxBlur(0, 0, 0));

                });

                deleteServerBtn.setMaxWidth(Double.MAX_VALUE);
                deleteServerOpenRootBtn.setMaxWidth(Double.MAX_VALUE);
                cancelButton.setMaxWidth(Double.MAX_VALUE);
                deleteServerBtn.setStyle(buttonRedStyle);
                deleteServerOpenRootBtn.setStyle(buttonRedStyle);
                cancelButton.setStyle(buttonStyle);
                mainBox.setPadding(boxPadding);
                mainBox.getChildren().addAll(deleteLabel, deleteServerBtn, deleteServerOpenRootBtn, cancelButton);
                mainBox.setStyle(dialogStyle);
                dialogScene = new Scene(new Group(mainBox));
                dialog.setScene(dialogScene);
                dialog.show();
            });

            //region config table
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
                try {
                    t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().setValue(t.getNewValue());
                    new Thread(() -> currentServer.saveServerConfig()).start();
                } catch (Exception e) {
                    configTable.refresh();
                }
            });
            valueCol.prefWidthProperty().bind(configTable.widthProperty().divide(2));
            valueCol.setEditable(true);

            configTable.getColumns().add(attrCol);
            configTable.getColumns().add(valueCol);
            ListChangeListener keepCol = new ListChangeListener() {
                boolean suspended;

                @Override
                public void onChanged(Change change) {
                    change.next();
                    if (change.wasReplaced() && !suspended) {
                        this.suspended = true;
                        configTable.getColumns().set(0, attrCol);
                        configTable.getColumns().set(1, valueCol);
                        this.suspended = false;
                    }
                }
            };
            configTable.getColumns().addListener(keepCol);


            configTable.setEditable(true);
            configTable.setShowRoot(false);
            configTable.sort();
            configTable.setMaxHeight(Double.MAX_VALUE);
            //endregion

            jarFileSelect.prefWidthProperty().bind(serverProperties.widthProperty().divide(2.25));
            jarFileSelect.setOnAction(event -> {
                try {
                    if (!jarFileSelect.getSelectionModel().getSelectedItem().equals(""))
                        currentServer.setServerFileName(jarFileSelect.getSelectionModel().getSelectedItem());
                } catch (Exception e) {
                    System.out.print(e.getMessage());
                }

            });
            serverProperties.getChildren().addAll(jarFileLabel, jarFileSelect, ramLabel, serverRamList, startServerBtn, renameServerBtn, deleteButtonBtn);
            serverProperties.setAlignment(Pos.CENTER);
            warningLabel.setMaxWidth(Double.MAX_VALUE);
            helpLabel.setAlignment(Pos.CENTER_RIGHT);
            helpLabel.setOnAction(event -> {
                HostServicesFactory.getInstance(application).showDocument("https://minecraft.gamepedia.com/Server.properties#Minecraft_server_properties");
            });
            labelBox.getChildren().addAll(warningLabel, helpLabel);

            bottomBox.getChildren().addAll(serverProperties, configTable, labelBox);
            //endregion

            this.getChildren().addAll(topBox, bottomBox);
            this.setPadding(new Insets(8));
            this.setSpacing(8);
        }

        private TreeItem<MinecraftServer.Config> loadConfig(MinecraftServer currentServer) {
            TreeItem<MinecraftServer.Config> root = new TreeItem<>(new MinecraftServer.Config("abc", "1", c -> true));
            currentServer.getConfigs().forEach(e -> {
                root.getChildren().add(new TreeItem<>(e));
            });
            return root;
        }
    }

    private Node[] setHGrow(Node... nodes) {
        Arrays.asList(nodes).forEach(e -> HBox.setHgrow(e, Priority.ALWAYS));
        return nodes;
    }

    private Node[] setVGrow(Node... nodes) {
        Arrays.asList(nodes).forEach(e -> VBox.setVgrow(e, Priority.ALWAYS));
        return nodes;
    }

    private Node[] setStyle(String style, Node... nodes) {
        Arrays.asList(nodes).forEach(e -> e.setStyle(style));
        return nodes;
    }

    private Node[] setPadding(Insets padding, Label... labels) {
        Arrays.asList(labels).forEach(e -> e.setPadding(padding));
        return labels;
    }

    private Node[] setPadding(Insets padding, HBox... HBox) {
        Arrays.asList(HBox).forEach(e -> e.setPadding(padding));
        return HBox;
    }

    private Node[] setPadding(Insets padding, VBox... VBox) {
        Arrays.asList(VBox).forEach(e -> e.setPadding(padding));
        return VBox;
    }

    private Node[] setDisable(boolean disable, Node... nodes) {
        Arrays.asList(nodes).forEach(e -> e.setDisable(disable));
        return nodes;
    }

    private Node[] setEditable(boolean editable, JFXTextField... jfxTextFields) {
        Arrays.asList(jfxTextFields).forEach(e -> e.setEditable(editable));
        return jfxTextFields;
    }
}

