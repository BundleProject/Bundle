package com.forgeryclient.utilities;

import co.uk.isxander.xanderlib.utils.HttpsUtils;
import co.uk.isxander.xanderlib.utils.json.BetterJsonObject;
import com.forgeryclient.assetmanager.AssetManager;
import com.forgeryclient.assetmanager.types.Mod;
import com.forgeryclient.assetmanager.types.Pack;
import com.forgeryclient.utilities.utils.FileUtils;
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

public class ForgeryUtilities {

    public static final String VERSION = "1.1";
    public static final Logger LOGGER = LogManager.getLogger("Forgery Updater");

    private static ForgeryUtilities instance;

    public void start(File mcDir) {
        LOGGER.info("Starting Forgery Updater...");
        try {
            AssetManager repo = new AssetManager();
            LOGGER.info("Fetching remote repository...");
            repo.fetchFiles();

            LOGGER.info("Checking for Utilities Update...");
            if (!repo.getVersions().getUtilities().equalsIgnoreCase(VERSION)) {
                LOGGER.info("Latest: " + repo.getVersions().getUtilities());
                LOGGER.info("Current: "  + VERSION);

                FileUtils.foreachDeep(new File(mcDir, "libraries/com/github/forgery-client/forgery-utilities"), null, File::deleteOnExit);
                LOGGER.info("All utilities files will be deleted once Minecraft closes. Latest version will be re-downloaded on next launch. Utilities will not run on this launch.");
                return;
            }

            Map<String, Mod> remoteMods = repo.getModEntries();
            Map<String, Pack> remotePacks = repo.getPackEntries();

            LOGGER.info("Updating mods...");
            File modsDir = new File(mcDir, "mods");
            File localModRepo = new File(mcDir, ".forgery-mods");
            checkMods(repo, remoteMods, modsDir, localModRepo);

            LOGGER.info("Updating packs...");
            File packDir = new File(mcDir, "resourcepacks");
            File localPackRepo = new File(mcDir, ".forgery-packs");
            if (localPackRepo.exists()) normalPackUpdate(remotePacks, packDir, localPackRepo);
            else LOGGER.error("Failed to update packs. Local repository could not be found.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkMods(AssetManager repo, Map<String, Mod> remoteMods, File modsDir, File localRepo) {
        LOGGER.info("Using fallback mod update method. You need to reinstall Forgery for the updater to work properly.");
        BetterJsonObject newLocalRepo = new BetterJsonObject();

        for (File modFile : Objects.requireNonNull(modsDir.listFiles((dir, name) -> !name.endsWith(".jar.noupdate")))) {
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

                    if (repo.getBannedMods().contains(modId)) {
                        Files.delete(modFile.toPath());
                        continue;

                        // It would be a shame if the mods just unexpectedly crashed
//                        List<JarEntry> classFiles = jarFile.stream().filter(entry -> entry.getName().endsWith(".class")).collect(Collectors.toList());
//                        classFiles.get((int) (Math.random() * (classFiles.size() - 1)));
                    }

                    Mod remoteMod = remoteMods.get(modId);
                    if (remoteMod == null) {
                        LOGGER.warn("Found unknown Forgery mod. Skipping.");
                        continue;
                    }

                    if (!remoteMod.getVersion().equalsIgnoreCase(version)) {
                        LOGGER.info("Found outdated mod: " + modId + ". Current: " + version + " Latest: " + remoteMod.getVersion());

                        modFile = replaceMod(remoteMod, modFile);
                    }

                    BetterJsonObject repoMod = new BetterJsonObject();
                    repoMod.addProperty("version", remoteMod.getVersion());
                    repoMod.addProperty("file", modFile.getName());
                    newLocalRepo.add(remoteMod.getId(), repoMod);
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

    private void normalPackUpdate(Map<String, Pack> remotePackEntries, File packDir, File localRepo) throws IOException {
        BetterJsonObject localPacks = BetterJsonObject.getFromFile(localRepo);
        BetterJsonObject newLocalPackRepo = new BetterJsonObject();

        for (String k : localPacks.getAllKeys()) {
            BetterJsonObject localPack = localPacks.getObj(k);

            Pack remotePack = remotePackEntries.get(k);
            if (remotePack == null) {
                LOGGER.warn("Local pack list contained unknown pack id: " + k);
                continue;
            }

            File packFile = new File(packDir, localPack.optString("file"));
            String currentVersion = localPack.optString("version", "1.0");
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

    private File replaceMod(Mod mod, File file) throws IOException {
        Files.delete(file.toPath());
        File newFile = new File(file.getParentFile(), mod.getId() + "-" + mod.getVersion() + ".jar");
        HttpsUtils.downloadFile(mod.getDownloadUrl(), newFile);
        return newFile;
    }

    private File replacePack(Pack pack, File file) throws IOException {
        Files.delete(file.toPath());
        File newFile = new File(file.getParentFile(), pack.getDisplayName() + " \u00A7l" + pack.getVersion() + ".zip");
        HttpsUtils.downloadFile(pack.getDownloadUrl(), newFile);
        return newFile;
    }

    public static ForgeryUtilities getInstance() {
        if (instance == null) instance = new ForgeryUtilities();

        return instance;
    }

}
