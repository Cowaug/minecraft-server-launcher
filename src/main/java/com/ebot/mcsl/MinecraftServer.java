package com.ebot.mcsl;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextArea;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class MinecraftServer {
    private Process proc;
    private String serverName;
    private String serverLocation;
    private int maxRam = 1024;
    private String serverFileName = "server.jar";
    private ArrayList<Config> configs = new ArrayList<>();

    MinecraftServer(String serverName, String serverLocation) {
        this.serverName = serverName;
        this.serverLocation = serverLocation;
        loadConfig();
    }

    String getServerLocation() {
        return serverLocation;
    }

    String getServerName() {
        return serverName;
    }

    public void startServer(final TextArea textArea) {
        new Thread(() -> {
            String command = "java -Xmx" + maxRam + "M -Xms" + (int) (maxRam * 0.25) + "M -XX:+UseG1GC -jar " + serverFileName + " nogui TRUE";
            try {
                proc = new ProcessBuilder(command.split(" ")).directory(new File(serverLocation)).start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(() -> textArea.appendText(finalLine + "\n"));
                    System.out.print(line + "\n");
                }
                reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(() -> textArea.appendText(finalLine + "\n"));
                    System.out.print(line + "\n");
                }

                textArea.setText("Server Closed.");
                proc.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void saveAndStop(final TextArea textArea) {
        if (!proc.isAlive()) return;
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
        try {
            writer.write("stop" + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void forceStop(final TextArea textArea) {
        proc.destroyForcibly();
        new Thread(new Runnable() {
            @Override
            public void run() {
                textArea.setText("Server Terminated.");
            }
        }).start();
    }

    public void loadConfig() {
        try (InputStream input = new FileInputStream(serverLocation + "\\" + "server.properties")) {
            CheckValue boolCheck = c -> Arrays.asList("true", "false").contains(c);
            CheckValue gameModeCheck = c -> Arrays.asList("creative", "survival", "spectator", "adventure").contains(c);
            CheckValue difficultyCheck = c -> Arrays.asList("peaceful", "easy", "normal", "hard").contains(c);
            CheckValue levelTypeCheck = c -> Arrays.asList("default", "flat", "largebiomes", "amplified", "buffet").contains(c);

            Properties prop = new Properties();
            prop.load(input);
            prop.forEach((k, v) -> {
                Config config;
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void saveConfig() {
        try (OutputStream output = new FileOutputStream(serverLocation + "\\" + "server.properties")) {
            Properties prop = new Properties();
            configs.forEach(e -> prop.setProperty(e.getAttribute(), e.getValue()));
            prop.store(output, null);
//            System.out.println(prop);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public ArrayList<Config> getConfigs() {
        return configs;
    }

    public static class Config extends RecursiveTreeObject<Config> {
        private StringProperty attribute;
        private StringProperty value;
        private String[] validAttr;
        private CheckValue checkValueFunction;

        public Config(String attribute, String value, CheckValue checkValueFunction) {
            this.attribute = new SimpleStringProperty(attribute);
            this.value = new SimpleStringProperty(value);
            this.checkValueFunction = checkValueFunction;
        }

        public Config(String attribute, String value) {
            this.attribute = new SimpleStringProperty(attribute);
            this.value = new SimpleStringProperty(value);
            this.validAttr = new String[]{};
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
