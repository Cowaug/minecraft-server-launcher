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

    /**
     * Get server from MCSL saved file
     */
    public static void scanServer() {
//        minecraftServers.clear();
//        File[] file = new File(path).listFiles(File::isDirectory);
//        Arrays.asList(file).forEach(e ->
//                minecraftServers.add(new MinecraftServer(e.getName(), e.getAbsolutePath()))
//        );
        InputStream in = null;
        try {
            in = new URL("https://raw.githubusercontent.com/Cowaug/minecraft-server-launcher/master/src/main/resources/version.txt").openStream();
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

    /**
     * Add exist server to database
     * @param name Server's name
     * @param path Path to server folder
     * @param serverFileName Server's execute .jar file
     */
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

    /**
     * Get list of all minecraft server
     * @return Array List of version
     */
    public static ArrayList<Version> getVersions() {
        return versions;
    }

    /**
     * Add new server
     * @param name Server's Name
     * @param path Path to server folder
     * @param version Version of server
     * @param jfxProgressBar Progress bar to display installation process
     * @return Success or not
     * @throws IOException Ex
     */
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

    /**
     * Get list of all server in database
     * @return
     */
    public static ArrayList<MinecraftServer> getMinecraftServers() {
        return minecraftServers;
    }

    /**
     * Delete server from database
     * @param minecraftServer
     */
    public static void removeMinecraftServer(MinecraftServer minecraftServer) {
        minecraftServers.remove(minecraftServer);
    }

    /**
     * Get Minecraft Server object from name
     * @param serverName Name of server
     * @return Minecraft Server object
     */
    public static MinecraftServer getMinecraftServer(String serverName) {
        return ((MinecraftServer) minecraftServers.stream().filter(e -> e.getServerName().equals(serverName)).toArray()[0]);
    }

    /**
     * Get list of all servers in database
     * @return String array of all servers
     */
    public static String[] getServerList() {
        ArrayList<String> arrayList = new ArrayList<>();
        minecraftServers.forEach(e -> arrayList.add(e.getServerName()));
        if (arrayList.size() == 0) return new String[]{};
        Collections.sort(arrayList);
        Collections.reverse(arrayList);
        return arrayList.toArray(new String[0]);
    }

    /**
     * Check if the server's name is valid
     * @param name Server's name
     * @return Valid or not
     */
    public static boolean isDuplicate(String name) {
        AtomicBoolean unValid = new AtomicBoolean(false);
        Arrays.asList("/", "\\", "*", ":", "?", "\"", "<", ">", "|").forEach(e -> {
            if (name.contains(e)) unValid.set(true);
        });
        return minecraftServers.stream().anyMatch(e -> e.getServerName().equals(name)) || unValid.get();
    }

    /**
     * Check if the server's name and path is valid
     * @param name Server's name
     * @param path Path to install
     * @return Valid or not
     */
    public static boolean isDuplicate(String name, String path) {
        AtomicBoolean unValid = new AtomicBoolean(false);
        Arrays.asList("/", "\\", "*", ":", "?", "\"", "<", ">", "|").forEach(e -> {
            if (name.contains(e)) unValid.set(true);
        });
        return minecraftServers.stream().anyMatch(e -> e.getServerName().equals(name)) ||
                unValid.get() ||
                minecraftServers.stream().anyMatch(e -> e.getServerLocation().equals(path));
    }

    /**
     * Force stop all running servers
     */
    public static void terminateAllServer() {
        minecraftServers.forEach(minecraftServer -> {
            try {
                minecraftServer.forceStop(null);
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * Version object
     */
    static class Version {
        String name;
        String url;

        /**
         * Create Version object
         * @param name Version's name
         * @param url Version's download URL
         */
        Version(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
}


