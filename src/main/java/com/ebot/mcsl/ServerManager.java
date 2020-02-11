package com.ebot.mcsl;


import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ServerManager {
    private static ArrayList<MinecraftServer> minecraftServers = new ArrayList<>();
    private static ArrayList<Version> versions = new ArrayList<>();

    public static void scanServer() {
//        minecraftServers.clear();
//        File[] file = new File(path).listFiles(File::isDirectory);
//        Arrays.asList(file).forEach(e ->
//                minecraftServers.add(new MinecraftServer(e.getName(), e.getAbsolutePath()))
//        );
        InputStream in = null;
        try {
            in = new URL("https://raw.githubusercontent.com/exos288/minecraft-server-launcher/master/src/main/resources/version.txt").openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Files.copy(in, Paths.get(Main.defaultPath + "\\" + "version.txt"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in = new FileInputStream(Main.defaultPath+"\\version.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader reader=new BufferedReader(new InputStreamReader(in));
        String line;
        while(true) {
            try {
                if ((line = reader.readLine()) == null) break;
                versions.add(new Version(line.split("\"")[1],line.split("\"")[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public static ArrayList<Version> getVersions() {
        return versions;
    }

    public static boolean addNewServer(String name, String path, String version, JFXProgressBar jfxProgressBar) throws IOException {
        File file = new File(path);
        if (file.mkdir()) {
            System.setProperty("http.agent", "Chrome");
            AtomicReference<String> url = new AtomicReference<>();
            versions.forEach(v->{
                if(v.name.equals(version.replace("Vanilla ", ""))) {
                    url.set(v.url);
                }
            });
            InputStream in = new URL(url.get()).openStream();
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
        Collections.sort(arrayList);
        Collections.reverse(arrayList);
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

    static class Version {
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


