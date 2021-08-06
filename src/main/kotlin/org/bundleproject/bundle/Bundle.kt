package org.bundleproject.bundle

import com.github.zafarkhaja.semver.Version
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import org.bundleproject.bundle.entities.Mod
import org.bundleproject.bundle.entities.platform.Platform
import org.bundleproject.bundle.utils.download
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import java.util.jar.JarFile

/**
 * The Bundle object holds the code to start the version checking
 * and update mods before the game is launched. Bundle works
 * by applying itself as a custom entrypoint
 * and does it's actions and then begins to launch the game
 * once it has finished.
 *
 * @author ChachyDev
 * @since 0.0.1
 */
object Bundle {
    /**
     * Starts the process by walking through the mods and looks
     * through each file and attempts to check if it's a valid mod
     * and if it's out of date then we begin the installation process
     * of redownloading the mod.
     *
     * @author ChachyDev
     * @since 0.0.1
     */
    suspend fun start(gameDir: File, version: Version, modFolderName: String) {
        try {
            val modsDir = File(gameDir, modFolderName)

            for (mod in modsDir.walkTopDown()) {
                val localMod = getModInfo(mod) ?: continue
                if (localMod.version != version) continue

                val remoteMod = Mod.fromUrl(localMod.latestUrl)

                // out of date
                if (remoteMod.version.greaterThan(localMod.version)) {
                    val parent = mod.parentFile
                    mod.delete()
                    runBlocking { URL(remoteMod.latestDownloadUrl) }
                        .download(File(parent, remoteMod.fileName))
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }

    private fun getModInfo(modFile: File): Mod? {
        JarFile(modFile).use { jar ->
            jar.getJarEntry("bundle.project.json")?.let getJarEntry@{ modInfo ->
                InputStreamReader(jar.getInputStream(modInfo)).use {
                    val json = JsonParser.parseReader(it).asJsonObject

                    if (json.has("update") && !json.get("update").asBoolean) return@getModInfo null

                    return@getModInfo Mod(
                        json.get("id")?.asString ?: return@getJarEntry,
                        Version.valueOf(json.get("version")?.asString ?: return@getJarEntry),
                        Version.valueOf(json.get("minecraft_version")?.asString ?: return@getJarEntry),
                        modFile.name,
                        Platform.valueOf(json.get("platform")?.asString ?: return@getJarEntry),
                    )
                }
            }

            jar.getJarEntry("fabric.mod.json")?.let { modInfo ->
                InputStreamReader(jar.getInputStream(modInfo)).use {
                    val json = JsonParser.parseReader(it).asJsonObject
                    return@getModInfo Mod(
                        json.get("id")?.asString ?: return null,
                        Version.valueOf(json.get("version")?.asString ?: return null),
                        Version.valueOf(json.get("depends").asJsonObject.get("minecraft")?.asString ?: return null),
                        modFile.name,
                        Platform.Fabric,
                    )
                }
            }

            jar.getJarEntry("mcmod.info")?.let { modInfo ->
                InputStreamReader(jar.getInputStream(modInfo)).use {
                    val json = JsonParser.parseReader(it).asJsonArray[0].asJsonObject
                    return@getModInfo Mod(
                        json.get("modid")?.asString ?: return null,
                        Version.valueOf(json.get("version")?.asString ?: return null),
                        Version.valueOf(json.get("mcversion")?.asString ?: return null),
                        modFile.name,
                        Platform.Forge,
                    )
                }
            }
        }

        return null
    }

}