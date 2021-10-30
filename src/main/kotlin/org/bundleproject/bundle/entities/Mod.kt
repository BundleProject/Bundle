package org.bundleproject.bundle.entities

import com.github.zafarkhaja.semver.Version
import org.bundleproject.bundle.api.data.ModData
import org.bundleproject.bundle.api.data.Platform
import org.bundleproject.bundle.api.requests.ModRequest

open class Mod(
    @Transient var enabled: Boolean = true,
    val name: String,
    val id: String,
    val version: Version,
    val minecraftVersion: Version,
    val fileName: String,
    val platform: Platform,
) {
    val downloadEndpoint = "${makeRequest().endpoint}/download"

    fun makeRequest(): ModRequest =
        ModRequest(id, platform, minecraftVersion.toString())

    fun applyData(data: ModData): RemoteMod {
        return RemoteMod(
            enabled,
            name,
            id,
            data.version,
            minecraftVersion,
            fileName,
            platform,
            data.url,
        )
    }

    suspend fun latest(): RemoteMod {
        return applyData(makeRequest().request())
    }

    operator fun compareTo(other: Mod): Int {
        return version.compareTo(other.version)
    }
}