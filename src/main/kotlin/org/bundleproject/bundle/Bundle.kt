package org.bundleproject.bundle

import com.formdev.flatlaf.FlatLightLaf
import com.github.zafarkhaja.semver.Version
import com.google.gson.JsonParser
import io.ktor.client.request.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.bundleproject.bundle.entities.Mod
import org.bundleproject.bundle.entities.platform.Platform
import org.bundleproject.bundle.gui.LoadingGui
import org.bundleproject.bundle.gui.UpdateOverviewGui
import org.bundleproject.bundle.utils.*
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import java.nio.file.Files
import java.util.concurrent.locks.ReentrantLock
import java.util.jar.JarFile
import javax.swing.*
import kotlin.concurrent.withLock

/**
 * The Bundle object holds the code to start the version checking
 * and update mods before the game is launched. Bundle works
 * by applying itself as a custom entrypoint
 * and does its actions and then begins to launch the game
 * once it has finished.
 *
 * @since 0.0.1
 */
class Bundle(private val gameDir: File, private val version: Version, modFolderName: String) {

    private val modsDir = File(gameDir, modFolderName)

    suspend fun start() {
        try {
            try { UIManager.setLookAndFeel(FlatLightLaf()) }
            catch (e: Throwable) { e.printStackTrace() }

            checkOutdated()
            val outdated = getOutdatedMods()

            if (outdated.isEmpty()) return

            val lock = ReentrantLock()
            val condition = lock.newCondition()
            lock.withLock {
                UpdateOverviewGui(this, outdated, condition)
                condition.await()
            }

        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * Walks through the mods and looks through each file
     * and attempts to check if it's a valid mod
     * and if it's out of date
     *
     * @since 0.0.1
     */
    private fun getOutdatedMods(): MutableList<Pair<Mod, Mod>> {
        val outdated = mutableListOf<Pair<Mod, Mod>>()
        for (mod in modsDir.walkTopDown()) {
            val localMod = getModInfo(mod) ?: continue
            if (localMod.version != version) continue

            val remoteMod = runBlocking { Mod.fromUrl(localMod.latestUrl) }

            // out of date
            if (remoteMod.version.greaterThan(localMod.version)) {
                outdated.add(Pair(localMod, remoteMod))
            }
        }

        return outdated
    }

    /**
     * Get metadata about a mod jar.
     *
     * @since 0.0.1
     */
    private fun getModInfo(modFile: File): Mod? {
        JarFile(modFile).use { jar ->
            jar.getJarEntry("bundle.project.json")?.let getJarEntry@{ modInfo ->
                InputStreamReader(jar.getInputStream(modInfo)).use {
                    val json = JsonParser.parseReader(it).asJsonObject

                    if (json.has("update") && !json.get("update").asBoolean) return@getModInfo null

                    return@getModInfo Mod(
                        name = json.get("name")?.asString ?: return null,
                        id = json.get("id")?.asString ?: return@getJarEntry,
                        version = Version.valueOf(json.get("version")?.asString ?: return@getJarEntry),
                        minecraftVersion = Version.valueOf(json.get("minecraft_version")?.asString ?: return@getJarEntry),
                        fileName = modFile.name,
                        platform = Platform.valueOf(json.get("platform")?.asString?.uppercase() ?: return@getJarEntry),
                    )
                }
            }

            jar.getJarEntry("fabric.mod.json")?.let { modInfo ->
                InputStreamReader(jar.getInputStream(modInfo)).use {
                    val json = JsonParser.parseReader(it).asJsonObject
                    return@getModInfo Mod(
                        name = json.get("name")?.asString ?: return null,
                        id = json.get("id")?.asString ?: return null,
                        version = Version.valueOf(json.get("version")?.asString ?: return null),
                        minecraftVersion = Version.valueOf(json.get("depends").asJsonObject.get("minecraft")?.asString ?: return null),
                        fileName = modFile.name,
                        platform = Platform.Fabric,
                    )
                }
            }

            jar.getJarEntry("mcmod.info")?.let { modInfo ->
                InputStreamReader(jar.getInputStream(modInfo)).use {
                    val json = JsonParser.parseReader(it).asJsonArray[0].asJsonObject
                    return@getModInfo Mod(
                        name = json.get("name")?.asString ?: return null,
                        id = json.get("modid")?.asString ?: return null,
                        version = Version.valueOf(json.get("version")?.asString ?: return null),
                        minecraftVersion = Version.valueOf(json.get("mcversion")?.asString ?: return null),
                        fileName = modFile.name,
                        platform = Platform.Forge,
                    )
                }
            }
        }

        return null
    }


    /**
     * Goes through a list of mods and linearly deletes
     * and replaces it with its updated counterpart
     *
     * @since 0.0.2
     */
    fun updateMods(mods: List<Pair<Mod, Mod>>) {
        launchCoroutine("Mod Updater") {
            val loading = LoadingGui(mods.size)
            loading.isVisible = true
            mods.map { (local, remote) ->
                async {
                    val current = File(modsDir, local.fileName)

                    Files.delete(current.toPath())
                    runBlocking { URL(remote.latestDownloadUrl) }
                        .download(File(modsDir, remote.fileName))
                    loading.finish()
                }
            }.awaitAll()
        }

    }

    /**
     * Checks if Bundle is outdated.
     *
     * @since 0.0.3
     */
    private suspend fun checkOutdated() {
        val versions = JsonParser.parseString(http.get<String>("$API/$API_VERSION/bundle/version")).asJsonObject

        if (Version.valueOf(versions.get("updater").asString).greaterThan(VERSION)) {
            JOptionPane.showMessageDialog(null, "Bundle is outdated. Please re-run the installer to get the update!", "Bundle", JOptionPane.WARNING_MESSAGE)
        }
    }

}