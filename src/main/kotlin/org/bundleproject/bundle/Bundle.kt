package org.bundleproject.bundle

import com.formdev.flatlaf.FlatLightLaf
import com.google.gson.JsonParser
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.bundleproject.bundle.entities.Mod
import org.bundleproject.bundle.api.data.Platform
import org.bundleproject.bundle.api.requests.BulkModRequest
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
class Bundle(private val gameDir: File, private val version: Version?, modFolderName: String) {
    private val modsDir = File(gameDir, modFolderName)

    constructor(gameDir: File, version: String?, modFolderName: String) : this(gameDir, version?.let(Version::of), modFolderName)

    suspend fun start() {
        try {
            info("Starting Bundle...")
            println()

            try { UIManager.setLookAndFeel(FlatLightLaf()) }
            catch (e: Throwable) { e.printStackTrace() }

            val outdated = getOutdatedMods()

            // return if outdated is empty
            if (outdated.isEmpty()) {
                info("No outdated mods found.")
                return
            }

//            val lock = ReentrantLock()
//            val condition = lock.newCondition()
//            lock.withLock {
//                UpdateOverviewGui(this, outdated, condition).apply {
//                    isVisible = true
//                }
//                condition.await()
//            }

            updateMods(outdated)

        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * Walks through the mods and looks through each file
     * and attempts to check if it's a valid mod
     *
     * All the valid mods are collected into a list before
     * bulk-requesting the latest versions from the API.
     * They are matched into pairs and returned.
     *
     * @since 0.0.1
     */
    private suspend fun getOutdatedMods(): MutableList<ModPair> {
        info("Getting outdated mods...")

        val localMods = mutableListOf<Mod>()
        for (mod in modsDir.walkTopDown()) {
            if (mod.isDirectory) continue

            val localMod = try {
                getModInfo(mod)
            } catch (e: Exception) {
                if (e is Version.VersionParseException) err("Failed to parse version for ${mod.name}")
                else e.printStackTrace()

                null
            } ?: continue

            if (localMod.minecraftVersion != (version ?: localMod.minecraftVersion)) continue

            localMods.add(localMod)
        }
        info("Found: ${localMods.map { it.id }.toFormattedString()}")
        println()

        info("Making bulk request to API.")
        val request = BulkModRequest(localMods.map { it.makeRequest() })
        val response = request.request()

        info("Comparing local to remote mod versions.")
        val outdated = mutableListOf<ModPair>()
        for (i in localMods.indices) {
            val local = localMods[i]
            val remote = local.applyData(response[i])

            if (remote > local)
                outdated.add(ModPair(local, remote))
        }
        info("${outdated.size}/${localMods.size} mods need an update.")
        info("Outdated: ${outdated.map { it.remote.id }.toFormattedString()}")
        println()

        return outdated
    }

    /**
     * Get metadata about a mod jar.
     *
     * @since 0.0.1
     */
    private fun getModInfo(modFile: File): Mod? {
        info("Getting mod info using method: ", false)
        JarFile(modFile).use { jar ->
            jar.getJarEntry("bundle.project.json")?.let getJarEntry@{ modInfo ->
                info("Bundle Info File")

                InputStreamReader(jar.getInputStream(modInfo)).use {
                    val json = JsonParser.parseReader(it).asJsonObject

                    if (json.has("update") && !json.get("update").asBoolean) return@getModInfo null

                    return@getModInfo Mod(
                        name = json.get("name")?.asString ?: return null,
                        id = json.get("id")?.asString ?: return@getJarEntry,
                        version = Version.of(json.get("version")?.asString ?: return@getJarEntry),
                        minecraftVersion = Version.of(json.get("minecraft_version")?.asString ?: return@getJarEntry),
                        fileName = modFile.name,
                        platform = Platform.valueOf(json.get("platform")?.asString?.uppercase() ?: return@getJarEntry),
                    )
                }
            }

            jar.getJarEntry("fabric.mod.json")?.let { modInfo ->
                info("Fabric Info File")

                InputStreamReader(jar.getInputStream(modInfo)).use {
                    val json = JsonParser.parseReader(it).asJsonObject
                    return@getModInfo Mod(
                        name = json.get("name")?.asString ?: return null,
                        id = json.get("id")?.asString ?: return null,
                        version = Version.of(json.get("version")?.asString ?: return null),
                        minecraftVersion = Version.of(json.get("depends").asJsonObject.get("minecraft")?.asString ?: return null),
                        fileName = modFile.name,
                        platform = Platform.Fabric,
                    )
                }
            }

            jar.getJarEntry("mcmod.info")?.let { modInfo ->
                info("Forge Info File")

                InputStreamReader(jar.getInputStream(modInfo)).use {
                    val json = JsonParser.parseReader(it).asJsonArray[0].asJsonObject
                    return@getModInfo Mod(
                        name = json.get("name")?.asString ?: return null,
                        id = json.get("modid")?.asString ?: return null,
                        version = Version.of(json.get("version")?.asString ?: return null),
                        minecraftVersion = Version.of(json.get("mcversion")?.asString ?: return null),
                        fileName = modFile.name,
                        platform = Platform.Forge,
                    )
                }
            }
        }

        err("None")
        return null
    }


    /**
     * Goes through a list of mods and asynchronously deletes
     * and replaces it with its updated counterpart
     *
     * @since 0.0.2
     */
    suspend fun updateMods(mods: List<ModPair>) {
        info("Updating Mods...")

        coroutineScope {
            val loading = LoadingGui(mods.size)
            loading.isVisible = true
            mods.map { (local, remote) ->
                async {
                    val current = File(modsDir, local.fileName)
                    val new = File(modsDir, remote.fileName)

                    info("Downloading: ${new.name}")
                    if (http.downloadFile(new, remote.downloadEndpoint)) {
                        info("Deleting: ${current.name}")
                        current.delete()
                    } else {
                        error("Failed to download! Skipping!")
                    }

                    loading.finish()
                }
            }.awaitAll()
        }
    }
}