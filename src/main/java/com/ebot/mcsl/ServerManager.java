package com.ebot.mcsl;


import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerManager {
    private static ArrayList<MinecraftServer> minecraftServers = new ArrayList<>();

    public static void scanServer(String path) {
//        minecraftServers.clear();
//        File[] file = new File(path).listFiles(File::isDirectory);
//        Arrays.asList(file).forEach(e ->
//                minecraftServers.add(new MinecraftServer(e.getName(), e.getAbsolutePath()))
//        );
        minecraftServers.addAll(UserConfig.readServerLocation());
    }

    public static void addExistServer(String name, String path, String serverFileName) {
        if (minecraftServers.stream().anyMatch(minecraftServer -> minecraftServer.getServerLocation().equals(path))) {
            minecraftServers.forEach(minecraftServer -> {
                if (minecraftServer.getServerLocation().equals(path)) {
                    minecraftServer.setServerName(name);
                    minecraftServer.setServerFileName(serverFileName);
                }
            });
        } else {
            minecraftServers.add(new MinecraftServer(name, path, serverFileName));
        }
    }

    public static boolean addNewServer(String name, String path, String version, JFXProgressBar jfxProgressBar) throws IOException {
        File file = new File(path);
        if (file.mkdir()) {
            System.setProperty("http.agent", "Chrome");
            InputStream in = new URL(Version.valueOf(version.replace("Vanilla ", "v").replace(".","")).getUrl()).openStream();
            Platform.runLater(()->jfxProgressBar.setProgress(0.0));
            Files.copy(ServerManager.class.getResourceAsStream("/eula.txt"), Paths.get(path + "\\" + "eula.txt"), StandardCopyOption.REPLACE_EXISTING);
            Platform.runLater(()->jfxProgressBar.setProgress(0.3));
            Files.copy(in, Paths.get(path + "\\" + "server.jar"), StandardCopyOption.REPLACE_EXISTING);
            Platform.runLater(()->jfxProgressBar.setProgress(0.7));
            Files.copy(ServerManager.class.getResourceAsStream("/server.properties"), Paths.get(path + "\\" + "server.properties"), StandardCopyOption.REPLACE_EXISTING);
            Platform.runLater(()->jfxProgressBar.setProgress(1.0));
            minecraftServers.add(new MinecraftServer(name, path));
            return true;
        } else {
            return false;
        }
    }

    public static ArrayList<MinecraftServer> getMinecraftServers() {
        return minecraftServers;
    }

    public static void removeMinecraftServer(MinecraftServer minecraftServer) {
        minecraftServers.remove(minecraftServer);
    }

    public static MinecraftServer getMinecraftServer(String serverName) {
        return ((MinecraftServer) minecraftServers.stream().filter(e -> e.getServerName().equals(serverName)).toArray()[0]);
    }

    public static String[] getServerList() {
        ArrayList<String> arrayList = new ArrayList<>();
        minecraftServers.forEach(e -> arrayList.add(e.getServerName()));
        if (arrayList.size() == 0) return new String[]{};
        return arrayList.toArray(new String[0]);
    }

    public static boolean isDuplicate(String name) {
        AtomicBoolean unValid = new AtomicBoolean(false);
        Arrays.asList("/", "\\", "*", ":", "?", "\"", "<", ">", "|").forEach(e -> {
            if (name.contains(e)) unValid.set(true);
        });
        return minecraftServers.stream().anyMatch(e -> e.getServerName().equals(name)) || unValid.get();
    }

    public static boolean isDuplicate(String name, String path) {
        AtomicBoolean unValid = new AtomicBoolean(false);
        Arrays.asList("/", "\\", "*", ":", "?", "\"", "<", ">", "|").forEach(e -> {
            if (name.contains(e)) unValid.set(true);
        });
        return minecraftServers.stream().anyMatch(e -> e.getServerName().equals(name)) ||
                unValid.get() ||
                minecraftServers.stream().anyMatch(e -> e.getServerLocation().equals(path));
    }

    public static void terminateAllServer() {
        minecraftServers.forEach(minecraftServer -> {
            try {
                minecraftServer.forceStop(null);
            } catch (Exception ignored) {
            }
        });
    }

    enum Version {
        v1151("1.15.1", "https://launcher.mojang.com/v1/objects/4d1826eebac84847c71a77f9349cc22afd0cf0a1/server.jar"),
        v115("1.15", "https://launcher.mojang.com/v1/objects/e9f105b3c5c7e85c7b445249a93362a22f62442d/server.jar"),
        v1_13_1("1.13", "y"),
        v1_12_1("1.12", "y"),
        v1_11_1("1.11", "y");
        String name;
        String url;

        Version(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }
}


