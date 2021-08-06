package org.bundleproject.bundle

import com.github.zafarkhaja.semver.Version
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import org.bundleproject.bundle.entities.Mod
import org.bundleproject.bundle.entities.platform.Platform
import org.bundleproject.bundle.utils.download
import org.bundleproject.bundle.utils.getResourceImage
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
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
 * @author ChachyDev
 * @since 0.0.1
 */
class Bundle(private val gameDir: File, private val version: Version, modFolderName: String) {

    private val modsDir = File(gameDir, modFolderName)

    suspend fun start() {
        try {
            openFrame(getOutdated())
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
    private fun getOutdated(): MutableList<Pair<Mod, Mod>> {
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
     * Allows the user to pick which mods they
     * would like to update and verify that
     * it is updating correctly.
     *
     * @since 0.0.2
     */
    private suspend fun openFrame(mods: MutableList<Pair<Mod, Mod>>) {
        val lock = ReentrantLock()
        val condition = lock.newCondition()

        lock.withLock {
            val frame = JFrame("Bundle")
            frame.iconImage = getResourceImage("/bundle.png")
            frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

            val gbl = GridBagLayout()
            val gbc = GridBagConstraints()
            frame.layout = gbl

            gbc.fill = GridBagConstraints.HORIZONTAL

            val rows = mutableListOf<Array<Any>>()
            for ((local, remote) in mods) {
                rows.add(arrayOf(
                    JCheckBox("", true).also { it.addActionListener { remote.enabled = false } },
                    remote.name,
                    local.version.toString(),
                    remote.version.toString(),
                    // TODO: 06/08/2021 get download url host because sk1er annoying
                ))
            }
            val table = JTable(rows.toTypedArray(), arrayOf("", "Mod", "Current", "Remote"))
            gbc.gridx = 0
            gbc.gridy = 0
            gbc.gridwidth = 2
            gbc.gridheight = 4
            frame.add(table, gbc)

            val skipButton = JButton("Skip")
            skipButton.addActionListener {
                mods.clear()
                frame.dispose()
                condition.signal()
            }
            gbc.gridx = 0
            gbc.gridy = 1
            gbc.gridwidth = 2
            gbc.gridheight = 1
            frame.add(skipButton, gbc)

            val downloadButton = JButton("Update")
            downloadButton.addActionListener {
                updateMods(mods.filter { it.second.enabled })
                frame.dispose()
                condition.signal()
            }
            gbc.gridx = 1
            gbc.gridy = 1
            gbc.gridwidth = 2
            gbc.gridheight = 1
            frame.add(downloadButton, gbc)
            condition.await()
        }
    }

    /**
     * Goes through a list of mods and linearly deletes
     * and replaces it with its updated counterpart
     *
     * @since 0.0.2
     */
    private fun updateMods(mods: List<Pair<Mod, Mod>>) {
        for ((local, remote) in mods) {
            val current = File(modsDir, local.fileName)

            Files.delete(current.toPath())
            runBlocking { URL(remote.latestDownloadUrl) }
                .download(File(modsDir, remote.fileName))
        }
    }

}