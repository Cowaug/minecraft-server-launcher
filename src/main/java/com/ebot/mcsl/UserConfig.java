package com.ebot.mcsl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class UserConfig {
    private static String userPath;

    public static String getUserPath() {
        return userPath;
    }

    public static void setUserPath(String userPath) {
        UserConfig.userPath = userPath;
        writeUserConfig(userPath);
    }

    public static void writeUserConfig(String userPath){
        UserConfig.userPath = userPath;
        JSONObject userConfig = new JSONObject();
        userConfig.put("path",userPath);

        try (FileWriter file = new FileWriter(Main.defaultPath + "\\" + "config.json")) {
            file.write(userConfig.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readUserConfig(){
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(Main.defaultPath + "\\" + "config.json")) {
            JSONObject userConfig = (JSONObject) jsonParser.parse(reader);
            userPath = (String) userConfig.get("path");
        } catch (Exception e) {
            e.printStackTrace();
            userPath = Main.defaultPath;
        }
    }

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
