package com.ebot.mcsl;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;

import java.io.*;
import java.util.*;

public class MinecraftServer {
    private Process proc;
    private String serverName;
    private String serverLocation;
    private int maxRam = 512;
    private String serverFileName = "server.jar";
    private ArrayList<Config> configs = new ArrayList<>();
    private Tab launchedTab = null;
    private ArrayList<String> jarFileList = new ArrayList<>();

    MinecraftServer(String serverName, String serverLocation) {
        this.serverName = serverName;
        this.serverLocation = serverLocation;
        loadServerConfig();
        loadLaunchConfig();
        loadJarList();
    }

    MinecraftServer(String serverName, String serverLocation, String serverFileName) {
        this.serverName = serverName;
        this.serverLocation = serverLocation;
        this.serverFileName = serverFileName;
        loadServerConfig();
        loadLaunchConfig();
        loadJarList();
    }

    public int getMaxRam() {
        return maxRam;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void renameServerLocation(String newName) {
        File file = new File(serverLocation);
        String newServerLocation = serverLocation.substring(0, serverLocation.lastIndexOf("\\")) + "\\" + newName;
        File newFile = new File(newServerLocation);
        if (file.renameTo(newFile)) {
            serverLocation = newServerLocation;
        }
    }

    public void setServerFileName(String serverFileName) {
        this.serverFileName = serverFileName;
        saveLaunchConfig();
    }

    public String getServerFileName() {
        return serverFileName;
    }

    void loadJarList() {
        File[] file = new File(serverLocation).listFiles(f -> f.isFile() && f.getName().endsWith(".jar"));
        if (file == null) return;
        Arrays.asList(file).forEach(e -> jarFileList.add(e.getName()));
    }

    public ArrayList<String> getJarFileList() {
        return jarFileList;
    }

    String getServerLocation() {
        return serverLocation;
    }

    String getServerName() {
        return serverName;
    }

    public Tab getLaunchedTab() {
        return launchedTab;
    }

    public boolean isLaunched() {
        return launchedTab != null;
    }

    public void setLaunchedTab(Tab launchedTab) {
        this.launchedTab = launchedTab;
    }

    public void startServer(final TextArea textArea, final JFXButton... buttons) {
        new Thread(() -> {
            String command = "java -Xmx" + maxRam + "M -Xms" + (int) (maxRam * 0.25) + "M -XX:+UseG1GC -jar " + serverFileName + " nogui TRUE";
            try {
                Platform.runLater(() -> textArea.setText("Starting server with " + maxRam + " MB of RAM...\n\n"));
                proc = new ProcessBuilder(command.split(" ")).directory(new File(serverLocation)).start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(() -> textArea.appendText(finalLine + "\n"));
                    System.out.println(line + "\n");
                }
                reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(() -> textArea.appendText(finalLine + "\n"));
                    System.out.println(line + "\n");
                }

                Platform.runLater(() -> {
                    textArea.setText("Server Closed.");
                    Arrays.asList(buttons).forEach(btn -> {
                        switch (btn.getText()) {
                            case "Close":
                            case "Start":
                                btn.setDisable(false);
                                break;
                            case "Terminate":
                            case "Stop":
                                btn.setDisable(true);
                                break;
                        }
                    });
                });
                proc.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void saveAndStop() {
        writeCmd("stop\n");
    }

    public void writeCmd(String cmd) {
        try {
            if (!proc.isAlive()) return;
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
            try {
                writer.write(cmd);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void forceStop(final TextArea textArea) {
        try {
            proc.destroyForcibly();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                textArea.setText("Server Terminated.");
//            }
//        }).start();
    }

    public void setMaxRam(int maxRam) {
        this.maxRam = maxRam;
        saveLaunchConfig();
    }

    public void loadServerConfig() {
        try (InputStream input = new FileInputStream(serverLocation + "\\" + "server.properties")) {
            CheckValue boolCheck = c -> Arrays.asList("true", "false").contains(c);
            CheckValue gameModeCheck = c -> Arrays.asList("creative", "survival", "spectator", "adventure").contains(c);
            CheckValue difficultyCheck = c -> Arrays.asList("peaceful", "easy", "normal", "hard").contains(c);
            CheckValue levelTypeCheck = c -> Arrays.asList("default", "flat", "largebiomes", "amplified", "buffet").contains(c);

            Properties prop = new Properties();
            prop.load(input);
            prop.forEach((k, v) -> {
                CheckValue checkValueFunction;
                switch (k.toString()) {
                    case "allow-flight":
                    case "allow-nether":
                    case "enable-command-block":
                    case "enable-query":
                    case "enable-rcon":
                    case "force-gamemode":
                    case "generate-structures":
                    case "hardcore":
                    case "online-mode":
                    case "prevent-proxy-connections":
                    case "pvp":
                    case "snooper-enabled":
                    case "spawn-animals":
                    case "spawn-monsters":
                    case "spawn-npcs":
                    case "use-native-transport":
                    case "white-list":
                    case "enforce-whitelist":
                        checkValueFunction = boolCheck;
                        break;

                    case "function-permission-level":
                    case "op-permission-level":
                        checkValueFunction = c -> Integer.parseInt(c) <= 4 && Integer.parseInt(c) >= 1;
                        break;

                    case "network-compression-threshold":
                        checkValueFunction = c -> Integer.parseInt(c) <= -1;
                        break;

                    case "max-build-height":
                        checkValueFunction = c -> Integer.parseInt(c) > 0;
                        break;

                    case "spawn-protection":
                    case "player-idle-timeout":
                    case "max-players":
                        checkValueFunction = c -> Integer.parseInt(c) >= 0;
                        break;

                    case "max-tick-time":
                        checkValueFunction = c -> Long.parseLong(c) > 0;
                        break;

                    case "max-world-size":
                        checkValueFunction = c -> Integer.parseInt(c) <= 29999984 && Integer.parseInt(c) >= 1;
                        break;

                    case "rcon.port":
                    case "query.port":
                    case "server-port":
                        checkValueFunction = c -> Short.parseShort(c) > 0;
                        break;

                    case "view-distance":
                        checkValueFunction = c -> Short.parseShort(c) >= 3 && Short.parseShort(c) <= 32;
                        break;

                    case "gamemode":
                        checkValueFunction = gameModeCheck;
                        break;

                    case "level-type":
                        checkValueFunction = levelTypeCheck;
                        break;

                    case "difficulty":
                        checkValueFunction = difficultyCheck;
                        break;

                    default:
                        checkValueFunction = c -> true;
                }
                configs.add(new Config(k.toString(), v.toString(), checkValueFunction));
            });
            configs.sort(Comparator.comparing(Config::getAttribute));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

    }


    public void saveServerConfig() {
        try (OutputStream output = new FileOutputStream(serverLocation + "\\" + "server.properties")) {
            Properties prop = new Properties();
            configs.forEach(e -> prop.setProperty(e.getAttribute(), e.getValue()));
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void loadLaunchConfig() {
        try (InputStream input = new FileInputStream(serverLocation + "\\" + "mcsl.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            prop.forEach((k, v) -> {
                switch (k.toString()) {
                    case "jar-file":
                        serverFileName = v.toString();
                        break;
                    case "max-ram":
                        maxRam = Integer.parseInt(v.toString());
                }
            });
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

    }

    public void saveLaunchConfig() {
        try (OutputStream output = new FileOutputStream(serverLocation + "\\" + "mcsl.properties")) {
            Properties prop = new Properties();
            prop.setProperty("jar-file", serverFileName);
            prop.setProperty("max-ram", String.valueOf(maxRam));
            prop.store(output, null);
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
    }

    public ArrayList<Config> getConfigs() {
        return configs;
    }

    public static class Config extends RecursiveTreeObject<Config> {
        private StringProperty attribute;
        private StringProperty value;
        private CheckValue checkValueFunction;

        public Config(String attribute, String value, CheckValue checkValueFunction) {
            this.attribute = new SimpleStringProperty(attribute);
            this.value = new SimpleStringProperty(value);
            this.checkValueFunction = checkValueFunction;
        }

        public String getAttribute() {
            return attribute.get();
        }

        public StringProperty getAttributeProp() {
            return attribute;
        }

        public String getValue() {
            return value.get();
        }

        public StringProperty getValueProp() {
            return value;
        }

        public void setValue(String value) throws Exception {
            if (checkValueFunction.check(value))
                this.value.set(value);
            else throw new Exception();
        }
    }

    interface CheckValue {
        boolean check(String s);
    }
}
