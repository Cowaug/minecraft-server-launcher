package com.ebot.mcsl;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.*;

public class MinecraftServer {
    private Process proc;
    private String serverName;
    private String serverLocation;
    private int maxRam = 1024;
    private String serverFileName = "server.jar";

    MinecraftServer(String serverName, String serverLocation) {
        this.serverName = serverName;
        this.serverLocation = serverLocation;
    }

    String getServerLocation() {
        return serverLocation;
    }

    String getServerName() {
        return serverName;
    }

    public void startServer(final TextArea textArea) {
        new Thread(() -> {
            String command = "java -Xmx" + maxRam + "M -Xms" + (int)(maxRam * 0.25) + "M -XX:+UseG1GC -jar " + serverFileName + " nogui TRUE";
            try {
                proc = new ProcessBuilder(command.split(" ")).directory(new File(serverLocation)).start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            textArea.appendText(finalLine + "\n");
                        }
                    });
                    System.out.print(line + "\n");
                }
                reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            textArea.appendText(finalLine + "\n");
                        }
                    });
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
}
