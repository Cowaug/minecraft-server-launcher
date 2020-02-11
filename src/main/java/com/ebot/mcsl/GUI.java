package com.ebot.mcsl;

import com.jfoenix.controls.*;
import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.KeyCode;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class GUI extends JFXTabPane {
    private Stage stage;
    private Application application;
    public static final String boxStyle = "-fx-border-color: #72737a20;\n" +
            "-fx-border-insets: 0;\n" +
            "-fx-border-width: 2;\n";
    public static final String boxStyle2 = "-fx-border-color: #72737a40;\n" +
            "-fx-border-insets: 0;\n" +
            "-fx-border-width: 2 2 0 2;\n";
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
    public static final String buttonGreyStyle =
            "    -fx-background-color: #72737a;\n" +
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
            JFXTextArea console = new JFXTextArea();

            JFXTextField commandLine = new JFXTextField();
            HBox btnBox = new HBox(8);
            JFXButton startBtn = new JFXButton("Start");
            JFXButton stopBtn = new JFXButton("Stop");
            JFXButton closeBtn = new JFXButton("Close");
            JFXButton terminateBtn = new JFXButton("Terminate");

            //region Style
            this.setSpacing(8);
            this.setPadding(new Insets(16));
            GUI.this.setStyle(buttonStyle, startBtn, stopBtn);
            GUI.this.setStyle(buttonRedStyle, terminateBtn, closeBtn);
            GUI.this.setHGrow(startBtn, stopBtn, closeBtn, terminateBtn, commandLine);
            GUI.this.setPadding(boxPadding, btnBox);

            setVGrow(console);
            console.setEditable(false);
            startBtn.setMaxWidth(Double.MAX_VALUE);
            stopBtn.setMaxWidth(Double.MAX_VALUE);
            closeBtn.setMaxWidth(Double.MAX_VALUE);
            terminateBtn.setMaxWidth(Double.MAX_VALUE);
            commandLine.setPromptText("Enter your command here");
            console.setStyle(boxStyle2);
            console.setPadding(boxPadding);
            //endregion
            commandLine.setOnKeyPressed(ke -> {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    minecraftServer.writeCmd(commandLine.getText()+"\n");
                    commandLine.setText("");
                }else if (ke.getCode().equals(KeyCode.TAB)) {
                    //todo auto fill
                }
            });

            stopBtn.setDisable(true);
            terminateBtn.setDisable(true);
            startBtn.setOnAction(event -> {
                minecraftServer.startServer(console, startBtn, stopBtn, closeBtn, terminateBtn);
                startBtn.setDisable(true);
                stopBtn.setDisable(false);
                terminateBtn.setDisable(false);
                closeBtn.setDisable(true);
            });
            stopBtn.setOnAction(event -> {
                minecraftServer.saveAndStop();
            });
            terminateBtn.setOnAction(event -> {
                minecraftServer.forceStop(console);
                terminateBtn.setDisable(true);
                stopBtn.setDisable(true);
                closeBtn.setDisable(false);
                startBtn.setDisable(false);
            });
            closeBtn.setOnAction(event -> {
                minecraftServer.setLaunchedTab(null);
                GUI.this.getTabs().remove(tab);
            });

            btnBox.getChildren().addAll(startBtn, stopBtn, terminateBtn, closeBtn);
            this.getChildren().addAll(console, commandLine, btnBox);
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
            Label serverPathLabel = new Label("Path ");
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
            JFXTextField searchField = new JFXTextField();
            JFXTreeTableView<MinecraftServer.Config> configTable = new JFXTreeTableView<>();

            HBox labelBox = new HBox(8);
            Label warningLabel = new Label("");
            Hyperlink helpLabel = new Hyperlink("What are these values mean?");
            //endregion

            //region Styles
            GUI.this.setPadding(labelPadding, selectServerLabel, serverPathLabel, jarFileLabel, warningLabel, ramLabel);
            GUI.this.setPadding(boxPadding, topBox, selectServerBox, addServerBox, serverProperties, labelBox);
            GUI.this.setPadding(boxPadding, bottomBox);
            GUI.this.setStyle(boxStyle, topBox, bottomBox);

            GUI.this.setStyle(buttonStyle, openServerLocationBtn, addExistServerBtn, addNewServerBtn, renameServerBtn, startServerBtn);
            GUI.this.setStyle(buttonRedStyle, deleteButtonBtn);
            GUI.this.setVGrow(configTable, bottomBox);
            GUI.this.setHGrow(selectServerLabel, serverList, openServerLocationBtn, serverPathLabel, serverPath, selectServerBox,
                    jarFileLabel, jarFileSelect, ramLabel, serverRamList, startServerBtn, renameServerBtn, deleteButtonBtn,
                    warningLabel, helpLabel, searchField);
            GUI.this.setEditable(false, serverPath);
            GUI.this.setDisable(true, bottomBox, openServerLocationBtn);
            searchField.setPromptText("Type attribute to search");
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
                configTable.setRoot(loadConfig(currentServer,searchField.getText()));
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
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initStyle(StageStyle.UNDECORATED);
                dialog.initOwner(stage);
                GUI.this.setEffect(new BoxBlur(4, 4, 4));

                Scene dialogScene;

                VBox mainBox = new VBox(8);
                HBox nameBox = new HBox(8);
                HBox pathBox = new HBox(8);
                HBox versionBox = new HBox(8);
                Label pathLabel = new Label("Path");
                pathLabel.setPadding(labelPadding);
                Label nameLabel = new Label("Server name");
                nameLabel.setPadding(labelPadding);
                Label versionLabel = new Label("Sever version");
                versionLabel.setPadding(labelPadding);
                JFXTextField pathField = new JFXTextField();
                JFXButton changeBtn = new JFXButton("Select folder");
                JFXTextField nameField = new JFXTextField("Server");
                JFXComboBox<String> versionList = new JFXComboBox<>();
                JFXButton confirmBtn = new JFXButton("Add");
                JFXButton cancelButton = new JFXButton("Cancel");
                Label notifyLabel = new Label("Not ready");
                AtomicBoolean preferServerVersion = new AtomicBoolean(true);
                JFXProgressBar jfxBar = new JFXProgressBar();
                jfxBar.setProgress(0.0);
                cancelButton.setOnAction(e -> {
                    dialog.close();
                    GUI.this.setEffect(new BoxBlur(0, 0, 0));

                });
                nameField.setOnKeyReleased(e -> {
                    preferServerVersion.set(false);
                    try {
                        if (ServerManager.isDuplicate(nameField.getText()) || nameField.getText().equals("") || pathField.getText().equals("")) {
                            confirmBtn.setDisable(true);
                            notifyLabel.setText("Duplicate / Invalid server name");
                        } else if (pathField.getText().equals("")) {
                            confirmBtn.setDisable(true);
                            notifyLabel.setText("Invalid install path");
                        } else {
                            confirmBtn.setDisable(false);
                            notifyLabel.setText("Ready");
                        }
                        pathField.setText((pathField.getText().substring(0, pathField.getText().lastIndexOf("\\")) + "\\" + nameField.getText()).replace(":\\\\", ":\\"));
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                });
                changeBtn.setOnAction(e -> {
                    File selectedDirectory;
                    try {
                        DirectoryChooser directoryChooser = new DirectoryChooser();
                        directoryChooser.setInitialDirectory(new File(pathField.getText().substring(0,pathField.getText().lastIndexOf("\\"))));
                        selectedDirectory = directoryChooser.showDialog(stage);
                    } catch (Exception ex) {
                        selectedDirectory = new DirectoryChooser().showDialog(stage);
                    }

                    if (selectedDirectory != null) {
                        pathField.setText((selectedDirectory.getAbsolutePath() + "\\" + nameField.getText()).replace(":\\\\", ":\\"));
                        confirmBtn.setDisable(ServerManager.isDuplicate(nameField.getText()) || nameField.getText().equals(""));
                        if (confirmBtn.isDisable()) {
                            notifyLabel.setText("Duplicate / Invalid server name");
                        } else {
                            notifyLabel.setText("Ready");
                        }
                    }
                });
                versionList.setOnAction(e -> {
                    if (preferServerVersion.get()) {
                        nameField.setText(versionList.getSelectionModel().getSelectedItem());
                        try {
                            if (ServerManager.isDuplicate(nameField.getText()) || nameField.getText().equals("") || pathField.getText().equals("")) {
                                confirmBtn.setDisable(true);
                                notifyLabel.setText("Duplicate / Invalid server name");
                            } else if (pathField.getText().equals("")) {
                                confirmBtn.setDisable(true);
                                notifyLabel.setText("Invalid install path");
                            } else {
                                confirmBtn.setDisable(false);
                                notifyLabel.setText("Ready");
                            }
                            pathField.setText((pathField.getText().substring(0, pathField.getText().lastIndexOf("\\")) + "\\" + nameField.getText()).replace(":\\\\", ":\\"));
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                });
                ServerManager.getVersions().forEach(v->{
                    versionList.getItems().add("Vanilla " + v.name);
                });

                versionList.getSelectionModel().select(0);
                {
                    nameField.setText(versionList.getSelectionModel().getSelectedItem());
                    confirmBtn.setDisable(ServerManager.isDuplicate(nameField.getText()));
                    if (confirmBtn.isDisable()) {
                        notifyLabel.setText("Duplicate / Invalid server name");
                    } else {
                        notifyLabel.setText("Ready");
                    }
                }
                confirmBtn.setDisable(true);
                confirmBtn.setOnAction(e -> {
                    new Thread(() -> {
                        try {
                            if (!ServerManager.addNewServer(nameField.getText(), pathField.getText(), versionList.getSelectionModel().getSelectedItem(), jfxBar))
                                throw new Exception();
                            Platform.runLater(() -> {
                                serverList.getItems().clear();
                                serverList.getItems().addAll(ServerManager.getServerList());
                                serverList.getSelectionModel().select(nameField.getText());
                                dialog.close();
                                UserConfig.writeServerLocation(ServerManager.getMinecraftServers());
                                UserConfig.setUserPath(pathField.getText().substring(0,pathField.getText().lastIndexOf("\\")));
                                GUI.this.setEffect(new BoxBlur(0, 0, 0));
                            });
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                            Platform.runLater(() -> {
                                notifyLabel.setText("Select other folder and try again");
                                cancelButton.setDisable(false);
                                versionList.setDisable(false);
                                nameBox.setDisable(false);
                                pathBox.setDisable(false);
                            });
                        }

                    }).start();
                    confirmBtn.setDisable(true);
                    cancelButton.setDisable(true);
                    versionList.setDisable(true);
                    nameBox.setDisable(true);
                    pathBox.setDisable(true);
                    notifyLabel.setText("Downloading... (may take long time on slow network)");

                });
                pathField.setText((UserConfig.getUserPath() + "\\" + nameField.getText()).replace(":\\\\", ":\\"));
                confirmBtn.setDisable(ServerManager.isDuplicate(nameField.getText()) || nameField.getText().equals(""));
                if (confirmBtn.isDisable()) {
                    notifyLabel.setText("Duplicate / Invalid server name");
                } else {
                    notifyLabel.setText("Ready");
                }
                confirmBtn.setMaxWidth(Double.MAX_VALUE);
                cancelButton.setMaxWidth(Double.MAX_VALUE);
                jfxBar.setMaxWidth(Double.MAX_VALUE);
                changeBtn.setStyle(buttonStyle);
                confirmBtn.setStyle(buttonStyle);
                cancelButton.setStyle(buttonGreyStyle);


                nameBox.getChildren().addAll(setHGrow(nameLabel, nameField));
                pathBox.getChildren().addAll(setHGrow(pathLabel, pathField, changeBtn));
                versionBox.getChildren().addAll(setHGrow(versionLabel, versionList));

                versionList.setMaxWidth(Double.MAX_VALUE);
                mainBox.setPadding(boxPadding);
                mainBox.getChildren().addAll(nameBox, pathBox, versionBox, confirmBtn, cancelButton, jfxBar, notifyLabel);
                mainBox.setStyle(dialogStyle);
                mainBox.setPrefWidth(GUI.this.getWidth() / 2);
                dialogScene = new Scene(new Group(mainBox));
                dialog.setScene(dialogScene);
                dialog.show();
            });
            addExistServerBtn.setOnAction(event -> {
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initStyle(StageStyle.UNDECORATED);
                dialog.initOwner(stage);
                GUI.this.setEffect(new BoxBlur(4, 4, 4));

                Scene dialogScene;

                VBox mainBox = new VBox(8);
                Label pathLabel = new Label("Path ");
                pathLabel.setPadding(labelPadding);
                Label jarLabel = new Label("Execute jar file");
                jarLabel.setPadding(labelPadding);
                pathLabel.setPadding(labelPadding);
                Label nameLabel = new Label("Server name");
                nameLabel.setPadding(labelPadding);
                JFXTextField pathField = new JFXTextField();
                JFXCheckBox sameAsFolder = new JFXCheckBox("Same as folder");
                sameAsFolder.setDisable(true);
                AtomicReference<String> oldName = new AtomicReference<>();
                JFXButton changeBtn = new JFXButton("Select folder");
                JFXTextField nameField = new JFXTextField("Server");
                JFXComboBox<String> jarList = new JFXComboBox<>();
                JFXButton confirmBtn = new JFXButton("Add");
                JFXButton cancelButton = new JFXButton("Cancel");


                cancelButton.setOnAction(e -> {
                    dialog.close();
                    GUI.this.setEffect(new BoxBlur(0, 0, 0));

                });
                sameAsFolder.setOnAction(e -> {
                    if (sameAsFolder.isSelected()) {
                        oldName.set(nameField.getText());
                        nameField.setText(pathField.getText().substring(pathField.getText().lastIndexOf("\\") + 1));
                    } else {
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
                    try {
                        DirectoryChooser directoryChooser = new DirectoryChooser();
                        directoryChooser.setInitialDirectory(new File(pathField.getText()));
                        selectedDirectory = directoryChooser.showDialog(stage);
                    } catch (Exception ex) {
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
                        if (jarList.getItems().contains("server.jar")) {
                            jarList.getSelectionModel().select("server.jar");
                        } else {
                            jarList.getSelectionModel().selectFirst();
                        }
                        confirmBtn.setDisable(ServerManager.isDuplicate(nameField.getText()));
                    }
                });
                confirmBtn.setDisable(true);
                confirmBtn.setOnAction(e -> {
                    ServerManager.addExistServer(nameField.getText(), pathField.getText(), jarList.getSelectionModel().getSelectedItem());
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
                cancelButton.setStyle(buttonGreyStyle);

                HBox nameBox = new HBox(8);
                nameBox.getChildren().addAll(setHGrow(nameLabel, nameField, sameAsFolder));

                HBox pathBox = new HBox(8);
                pathBox.getChildren().addAll(setHGrow(pathLabel, pathField, changeBtn));

                HBox jarBox = new HBox(8);
                jarBox.getChildren().addAll(setHGrow(jarLabel, jarList));
                jarList.setMaxWidth(Double.MAX_VALUE);

                mainBox.setPadding(boxPadding);
                mainBox.getChildren().addAll(nameBox, pathBox, jarBox, confirmBtn, cancelButton);
                mainBox.setStyle(dialogStyle);
                mainBox.setPrefWidth(GUI.this.getWidth() / 2);
                dialogScene = new Scene(new Group(mainBox));
                dialog.setScene(dialogScene);
                dialog.show();
            });
            selectServerBox.getChildren().addAll(selectServerLabel, serverList, openServerLocationBtn, serverPathLabel, serverPath);
            addServerBox.getChildren().addAll(addNewServerBtn, addExistServerBtn);
            topBox.getChildren().addAll(selectServerBox, addServerBox);
            //endregion

            //region Bottom Box
            for (float i = 0.25f; i <= 16; i = i + 0.25f) {
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
                warningLabel.setText("Change will not affected unless you reboot the server");
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
                cancelButton.setStyle(buttonGreyStyle);
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
                cancelButton.setStyle(buttonGreyStyle);
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
            searchField.setOnKeyPressed(event -> {
                configTable.setRoot(loadConfig(currentServer,searchField.getText()));
                configTable.setPredicate(attr -> attr.getValue().getAttribute().contains(searchField.getText()));
            });
            searchField.setOnMouseClicked(e -> searchField.selectAll());
            jarFileSelect.prefWidthProperty().bind(serverProperties.widthProperty().divide(2.25));
            jarFileSelect.setOnAction(event -> {
                try {
                    if (!jarFileSelect.getSelectionModel().getSelectedItem().equals(""))
                        currentServer.setServerFileName(jarFileSelect.getSelectionModel().getSelectedItem());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
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

            bottomBox.getChildren().addAll(serverProperties, searchField, configTable, labelBox);
            //endregion

            this.getChildren().addAll(topBox, bottomBox);
            this.setPadding(new Insets(16));
            this.setSpacing(8);
        }

        private TreeItem<MinecraftServer.Config> loadConfig(MinecraftServer currentServer, String contains) {
            TreeItem<MinecraftServer.Config> root = new TreeItem<>(new MinecraftServer.Config("abc", "1", c -> true));
            currentServer.getConfigs().forEach(e -> {
                if (e.getAttribute().contains(contains))
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

