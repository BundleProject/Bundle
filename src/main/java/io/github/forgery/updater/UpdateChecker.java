package io.github.forgery.updater;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    static Gson gson = new Gson();

    public static void updateMods() {
        File[] modList = new File(Launch.minecraftHome, "mods").listFiles((dir, name) -> name.endsWith(".jar"));
        assert modList != null; //make mod work in dev env when no mods are in the folder
        if (!modList[0].toString().equals("!Forgery-Updater-DONOTRENAME") || !modList[0].toString().equals("!Forgery-Updater-DONOTRENAME.jar")) { //if the updater isn't loaded first, throw a RuntimeException
            throw new RuntimeException("You have renamed the Forgery Updater or one of the files in your mod folder! In order for the updater to work, you need the Forgery Updater to be listed first in your mods folder (in alphabetical order).");
        }
        for (ModContainer container : Loader.instance().getModList()) {
            //insert code here.
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
