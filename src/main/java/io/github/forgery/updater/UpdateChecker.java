package io.github.forgery.updater;

import com.google.gson.*;
import net.minecraft.launchwrapper.Launch;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UpdateChecker {

    static Gson gson = new Gson();
    static ArrayList<JsonObject> modsFetched = new ArrayList<>();

    public static void updateMods() {
        File[] modList = new File(Launch.minecraftHome, "mods").listFiles((dir, name) -> name.endsWith(".jar"));
        assert modList != null;
        fetchFromGithub("mods", modsFetched); //this fetches mods.json from Forgery Assets
        for (JsonObject o : modsFetched) {
            for (File file : modList) {
                try {
                    try (ZipFile zipFile = new ZipFile(file)) {
                        ZipEntry entry = zipFile.getEntry("mcmod.info");
                        if (entry != null) {
                            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                                byte[] availableBytes = new byte[inputStream.available()];
                                inputStream.read(availableBytes, 0, inputStream.available());
                                JsonObject modInfo = (new JsonParser()).parse(new String(availableBytes)).getAsJsonArray().get(0).getAsJsonObject();
                                if (modInfo.has("modid")) {
                                    String modId = modInfo.get("modid").getAsString();
                                    if (modId.equals(o.get("id")) && modInfo.has("version") && !modInfo.get("version").getAsString().equalsIgnoreCase(o.get("version").getAsString())) {
                                        file.deleteOnExit();
                                        //add downloading code here
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void fetchFromGithub(String get, ArrayList<JsonObject> a) {
        JsonArray fetchedList = getJSONResponse("https://github.com/Forgery-Client/Forgery-Client-Assets/blob/main/assets/" + get + ".json").getAsJsonArray(); //we love abusing github
        for (JsonElement e : fetchedList) {
            for (JsonElement entries : e.getAsJsonObject().get("entries").getAsJsonArray()) {
                a.add(entries.getAsJsonObject());
            }
        }
    }
    /**
     * Adapted from Danker's Skyblock Mod under GPL 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     *
     * @author bowser0000
     */
    public static JsonObject getJSONResponse(String urlString) {

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String input;
                StringBuilder response = new StringBuilder();

                while ((input = in.readLine()) != null) {
                    response.append(input);
                }
                in.close();
                return gson.fromJson(response.toString(), JsonObject.class);
            } else {
                    System.out.println("Request failed. HTTP Error Code: " + conn.getResponseCode());
                }
        } catch (IOException ex) {
            System.out.println("An error has occured.");
            ex.printStackTrace();
        }

        return new JsonObject();
    }

}
