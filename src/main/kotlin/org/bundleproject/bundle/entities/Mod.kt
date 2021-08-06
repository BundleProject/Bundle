package org.bundleproject.bundle.entities

import com.github.zafarkhaja.semver.Version
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.bundleproject.bundle.Bundle

data class Mod(
    val id: String,
    val version: String,
    val minecraftVersion: String,
    val fileName: String,
    @Transient val platform: Platform,
) {

    @Transient val semver = Version.valueOf(version)

    @Transient val latestUrl = "https://api.bundle.isxander.dev/v1/mods/$platform/$id/$minecraftVersion/latest/"
    @Transient val latestDownloadUrl = latestUrl + "download"

    companion object {
        fun fromJson(json: String): Mod {
            return Gson().fromJson(json, Mod::class.java)
        }

        fun fromUrl(url: String): Mod {
            return runBlocking { fromJson(Bundle.HTTP_CLIENT.get(url)) }
        }
    }

}