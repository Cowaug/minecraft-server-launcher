package com.ebot.mcsl;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.*;

public class ServerConsole {
    private Process proc;

    public void run(String cmd, final TextArea textArea) {
        final String path = "D:\\Games Files\\Minecraft Server\\Vanilla 1.15.1";
        final String command = "java -Xmx8192M -Xms2048M -XX:+UseG1GC -jar server.jar nogui TRUE";

        new Thread(new Runnable() {
            public void run() {
                try {
                    proc = new ProcessBuilder(command.split(" ")).directory(new File(path)).start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line;
                        Platform.runLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                textArea.appendText(finalLine + "\n");
                            }
                        });
                        System.out.print(line + "\n");
                    }
                    reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line;
                        Platform.runLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                textArea.appendText(finalLine + "\n");
                            }
                        });
                        System.out.print(line + "\n");
                    }


                    proc.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stop() {

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
        try {
            writer.write("stop"+"\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void fstop() {
     proc.destroyForcibly();
    }
}
