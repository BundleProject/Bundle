package org.bundleproject.bundle.entities

import com.github.zafarkhaja.semver.Version
import io.ktor.client.request.*
import org.bundleproject.bundle.entities.platform.Platform
import org.bundleproject.bundle.utils.API
import org.bundleproject.bundle.utils.API_VERSION
import org.bundleproject.bundle.utils.http

data class Mod(
    val id: String,
    val version: Version,
    val minecraftVersion: Version,
    val fileName: String,
    @Transient val platform: Platform,
) {
    @Transient
    val latestUrl = "$API/$API_VERSION/mods/$id/$platform/$minecraftVersion/latest/"
    @Transient
    val latestDownloadUrl = latestUrl + "download"

    companion object {
        suspend fun fromUrl(url: String): Mod {
            return http.get(url)
        }
    }

}