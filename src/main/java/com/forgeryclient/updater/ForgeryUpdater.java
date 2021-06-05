package com.forgeryclient.updater;

import co.uk.isxander.xanderlib.utils.HttpsUtils;
import co.uk.isxander.xanderlib.utils.json.BetterJsonObject;
import com.forgeryclient.installer.repo.RepositoryManager;
import com.forgeryclient.installer.repo.entry.ModEntry;
import com.forgeryclient.installer.repo.entry.PackEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ForgeryUpdater {

    public static final Logger LOGGER = LogManager.getLogger("Forgery Updater");

    private static ForgeryUpdater instance;

    public void start(File mcDir) {
        try {
            RepositoryManager repo = new RepositoryManager();
            LOGGER.info("Fetching remote repository...");
            repo.fetchFiles();
            Map<String, ModEntry> remoteMods = repo.getModEntries();
            Map<String, PackEntry> remotePacks = repo.getPackEntries();

            LOGGER.info("Updating mods...");
            File modsDir = new File(mcDir, "mods");
            File localModRepo = new File(mcDir, ".forgery-mods");
//            if (localModRepo.exists()) normalModUpdate(remoteMods, localModRepo, modsDir);
//            else fallbackModUpdate(remoteMods, modsDir, localModRepo);
            fallbackModUpdate(remoteMods, modsDir, localModRepo);

            LOGGER.info("Updating packs...");
            File packDir = new File(mcDir, "resourcepacks");
            File localPackRepo = new File(mcDir, ".forgery-packs");
            if (localPackRepo.exists()) normalPackUpdate(remotePacks, packDir, localPackRepo);
            else LOGGER.error("Failed to update packs. Local repository could not be found.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void normalModUpdate(Map<String, ModEntry> remoteModEntries, File localRepo, File modsDir) throws IOException {
        BetterJsonObject localMods = BetterJsonObject.getFromFile(localRepo);
        BetterJsonObject newRepo = new BetterJsonObject();

        for (String k : localMods.getAllKeys()) {
            BetterJsonObject localModEntry = localMods.getObj(k);

            ModEntry remoteMod = remoteModEntries.get(k);
            if (remoteMod == null) {
                LOGGER.warn("Local mod list contained unknown mod id: " + k);
                continue;
            }

            File modFile = new File(modsDir, localModEntry.optString("file"));
            if (!remoteMod.getVersion().equalsIgnoreCase(localModEntry.optString("version", "1.0"))) {
                LOGGER.info("Found outdated mod: " + remoteMod.getDisplayName());

                modFile = replaceMod(remoteMod, modFile);
            }

            BetterJsonObject repoModEntry = new BetterJsonObject();
            repoModEntry.addProperty("version", remoteMod.getVersion());
            repoModEntry.addProperty("file", modFile.getName());
            newRepo.add(remoteMod.getId(), repoModEntry);
        }

        newRepo.writeToFile(localRepo);
    }

    private void fallbackModUpdate(Map<String, ModEntry> remoteMods, File modsDir, File localRepo) {
        LOGGER.info("Using fallback mod update method. You need to reinstall Forgery for the updater to work properly.");
        BetterJsonObject newLocalRepo = new BetterJsonObject();

        for (File modFile : Objects.requireNonNull(modsDir.listFiles((dir, name) -> !name.endsWith(".disabled")))) {
            try (JarFile jarFile = new JarFile(modFile)) {
                ZipEntry modInfo = jarFile.getEntry("mcmod.info");
                if (modInfo != null) {
                    InputStream is = jarFile.getInputStream(modInfo);
                    byte[] bytes = new byte[is.available()];
                    is.read(bytes, 0, is.available());
                    is.close();

                    BetterJsonObject modInfoJson = new BetterJsonObject(new String(bytes));
                    String modId = modInfoJson.optString("modid", "unknown");
                    String version = modInfoJson.optString("version", "1.0");

                    ModEntry remoteMod = remoteMods.get(modId);
                    if (remoteMod == null) {
                        LOGGER.warn("Found unknown Forgery mod. Skipping.");
                        continue;
                    }

                    if (!remoteMod.getVersion().equalsIgnoreCase(version)) {
                        LOGGER.info("Found outdated mod: " + modId + ". Current: " + version + " Latest: " + remoteMod.getVersion());

                        modFile = replaceMod(remoteMod, modFile);
                    }

                    BetterJsonObject repoModEntry = new BetterJsonObject();
                    repoModEntry.addProperty("version", remoteMod.getVersion());
                    repoModEntry.addProperty("file", modFile.getName());
                    newLocalRepo.add(remoteMod.getId(), repoModEntry);
                }
            } catch (IOException e) {
                LOGGER.warn("-----------------------------------------------------");
                LOGGER.warn("Encountered IO Exception when trying to read: " + modFile.getName() + ". If this is not a valid mod, please ignore this.");
                LOGGER.warn("");
                e.printStackTrace();
                LOGGER.warn("-----------------------------------------------------");
            }
        }
        newLocalRepo.writeToFile(localRepo);
    }

    private void normalPackUpdate(Map<String, PackEntry> remotePackEntries, File packDir, File localRepo) throws IOException {
        BetterJsonObject localPacks = BetterJsonObject.getFromFile(localRepo);
        BetterJsonObject newLocalPackRepo = new BetterJsonObject();

        for (String k : localPacks.getAllKeys()) {
            BetterJsonObject localPackEntry = localPacks.getObj(k);

            PackEntry remotePack = remotePackEntries.get(k);
            if (remotePack == null) {
                LOGGER.warn("Local pack list contained unknown pack id: " + k);
                continue;
            }

            File packFile = new File(packDir, localPackEntry.optString("file"));
            String currentVersion = localPackEntry.optString("version", "1.0");
            if (!remotePack.getVersion().equalsIgnoreCase(currentVersion)) {
                LOGGER.warn("Found outdated pack: " + remotePack.getId() + ". Current: " + currentVersion + " Latest: " + remotePack.getVersion());

                packFile = replacePack(remotePack, packFile);
            }

            BetterJsonObject repoEntry = new BetterJsonObject();
            repoEntry.addProperty("version", remotePack.getVersion());
            repoEntry.addProperty("file", packFile.getName());
            newLocalPackRepo.add(remotePack.getId(), repoEntry);
        }

        newLocalPackRepo.writeToFile(localRepo);
    }

    private File replaceMod(ModEntry mod, File file) throws IOException {
        Files.delete(file.toPath());
        File newFile = new File(file.getParentFile(), mod.getId() + "-" + mod.getVersion() + ".jar");
        HttpsUtils.downloadFile(mod.getDownloadUrl(), newFile);
        return newFile;
    }

    private File replacePack(PackEntry pack, File file) throws IOException {
        Files.delete(file.toPath());
        File newFile = new File(file.getParentFile(), pack.getDisplayName() + " \u00A7l" + pack.getVersion() + ".zip");
        HttpsUtils.downloadFile(pack.getDownloadUrl(), newFile);
        return newFile;
    }

    public static ForgeryUpdater getInstance() {
        if (instance == null) instance = new ForgeryUpdater();

        return instance;
    }

}
