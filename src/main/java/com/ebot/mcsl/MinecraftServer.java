package com.ebot.mcsl;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextArea;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
            List<String> gameMode = Arrays.asList("creative", "survival", "spectator", "adventure");
            List<String> bool = Arrays.asList("true", "false");
            List<String> difficulty = Arrays.asList("peaceful", "easy", "normal", "hard");
            List<String> levelType = Arrays.asList("default", "flat", "largebiomes", "amplified","buffet");

            Properties prop = new Properties();
            prop.load(input);
            prop.forEach((k, v) -> {
                if (gameMode.contains(v.toString())) {
                    configs.add(new Config(k.toString(), v.toString(), gameMode.toArray(new String[]{})));
                } else if (bool.contains(v.toString())) {
                    configs.add(new Config(k.toString(), v.toString(), bool.toArray(new String[]{})));
                } else if (difficulty.contains(v.toString())) {
                    configs.add(new Config(k.toString(), v.toString(), difficulty.toArray(new String[]{})));
                } else if (levelType.contains(v.toString())) {
                    configs.add(new Config(k.toString(), v.toString(), levelType.toArray(new String[]{})));
                } else try {
                    Integer.parseInt(v.toString());
                    configs.add(new Config(k.toString(), v.toString(), new String[]{"-1"}));
                } catch (Exception e) {
//                    e.printStackTrace();
                    configs.add(new Config(k.toString(), v.toString()));
                }
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

        public Config(String attribute, String value, String[] validAttr) {
            this.attribute = new SimpleStringProperty(attribute);
            this.value = new SimpleStringProperty(value);
            this.validAttr = validAttr;
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
            List<String> list = Arrays.asList(validAttr);
            if (list.contains("-1")) {
                Integer.parseInt(value);
                this.value.set(value);
            } else if (!list.contains(value) && list.size() > 1) {
                StringBuilder string = new StringBuilder();
                for (String s : list) {
                    string.append(s).append(" ");
                }
                throw new Exception("Expect: " + string);
            } else
                this.value.set(value);
        }
    }
}
