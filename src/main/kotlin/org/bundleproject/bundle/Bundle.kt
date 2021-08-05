package org.bundleproject.bundle

import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.bundleproject.bundle.entities.*
import org.bundleproject.bundle.utils.FileUtils
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.util.jar.JarFile

object Bundle {

    val HTTP_CLIENT = HttpClient(CIO)
    val PLATFORM = identifyPlatform()

    private lateinit var modsDir: File

    fun start(gameDir: File) {
        println("Starting Bundle...")
        println("By Xander, Chachy and Wyvest")

        modsDir = File(gameDir, "mods")

        for (modFile in FileUtils.foreachFileDeep(modsDir)) {
            val localMod = getModInfo(modFile) ?: continue
            if (localMod.platform != PLATFORM) continue

            val remoteMod = Mod.fromUrl(localMod.latestUrl)

            // out of date
            if (remoteMod.semver.greaterThan(localMod.semver)) {
                val parent = modFile.parentFile
                Files.delete(modFile.toPath())
                Files.write(File(parent, remoteMod.fileName).toPath(), runBlocking { HTTP_CLIENT.get<ByteArray>(remoteMod.latestDownloadUrl) })
            }
        }
    }

    private fun getModInfo(modFile: File): Mod? {
        JarFile(modFile).use { jar ->
            if (jar.manifest.mainAttributes.getValue("Bundle-No-Update") != null)
                return null

            jar.getJarEntry("fabric.mod.json")?.let { modInfo ->
                InputStreamReader(jar.getInputStream(modInfo)).use {
                    val json = JsonParser.parseReader(it).asJsonObject
                    return@getModInfo Mod(
                        json.get("id")?.asString ?: return null,
                        json.get("version")?.asString ?: return null,
                        json.get("depends").asJsonObject.get("minecraft")?.asString ?: return null,
                        modFile.name,
                        true,
                        Platform.FABRIC
                    )
                }
            }

            jar.getJarEntry("mcmod.info")?.let { modInfo ->
                InputStreamReader(jar.getInputStream(modInfo)).use {
                    val json = JsonParser.parseReader(it).asJsonArray[0].asJsonObject
                    return@getModInfo Mod(
                        json.get("modid")?.asString ?: return null,
                        json.get("version")?.asString ?: return null,
                        json.get("mcversion")?.asString ?: return null,
                        modFile.name,
                        true,
                        Platform.FORGE
                    )
                }
            }
        }

        return null
    }

    private fun identifyPlatform(): Platform {
        // TODO: 05/08/2021 maybe parameter from entrypoint?
        return Platform.FORGE
    }

}