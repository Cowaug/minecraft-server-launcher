package com.ebot.mcsl;


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

    public static ArrayList<MinecraftServer> getMinecraftServers() {
        return minecraftServers;
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

}
