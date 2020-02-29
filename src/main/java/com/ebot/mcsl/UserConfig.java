package com.ebot.mcsl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class UserConfig {
    private static String previousInstallationPath;

    /**
     * Get previous installation path
     * @return Previous installation path
     */
    public static String getPreviousInstallationPath() {
        return previousInstallationPath;
    }

    /**
     * Set previous installation path
     * @param previousInstallationPath Previous installation path
     */
    public static void setPreviousInstallationPath(String previousInstallationPath) {
        UserConfig.previousInstallationPath = previousInstallationPath;
        writeUserConfig(previousInstallationPath);
    }

    /**
     * Save user config
     * @param userPath Previous path of installation
     */
    public static void writeUserConfig(String userPath){
        UserConfig.previousInstallationPath = userPath;
        JSONObject userConfig = new JSONObject();
        userConfig.put("path",userPath);

        try (FileWriter file = new FileWriter(Main.defaultPath + "\\" + "config.json")) {
            file.write(userConfig.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load user config
     */
    public static void readUserConfig(){
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(Main.defaultPath + "\\" + "config.json")) {
            JSONObject userConfig = (JSONObject) jsonParser.parse(reader);
            previousInstallationPath = (String) userConfig.get("path");
        } catch (Exception e) {
            e.printStackTrace();
            previousInstallationPath = Main.defaultPath;
        }
    }

    /**
     * Save information about all server (name and it's location on disk)
     * @param serverLocations List of Minecraft Servers
     */
    public static void writeServerLocation(ArrayList<MinecraftServer> serverLocations) {
        JSONArray serverList = new JSONArray();
        serverLocations.forEach(serverLocation -> {
            JSONObject serverDetail = new JSONObject();
            serverDetail.put("name", serverLocation.getServerName());
            serverDetail.put("location", serverLocation.getServerLocation());
            JSONObject serverObject = new JSONObject();
            serverObject.put("server", serverDetail);
            serverList.add(serverObject);
        });

        try (FileWriter file = new FileWriter(Main.defaultPath + "\\" + "serverList.json")) {
            file.write(serverList.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load information about all server (name and it's location on disk)
     * @return  serverLocations List of Minecraft Servers
     */
    public static ArrayList<MinecraftServer> readServerLocation() {
        JSONParser jsonParser = new JSONParser();
        ArrayList<MinecraftServer> arrayList = new ArrayList<>();

        try (FileReader reader = new FileReader(Main.defaultPath + "\\" + "serverList.json")) {
            JSONArray serverList = (JSONArray) jsonParser.parse(reader);
            serverList.forEach(server -> arrayList.add(new MinecraftServer(
                    (String) ((JSONObject) ((JSONObject) server).get("server")).get("name"),
                    (String) ((JSONObject) ((JSONObject) server).get("server")).get("location"))
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }
}
