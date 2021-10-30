package org.bundleproject.bundle.api.requests

import io.ktor.client.request.*
import org.bundleproject.bundle.api.data.ModData
import org.bundleproject.bundle.api.responses.ModResponse
import org.bundleproject.bundle.api.data.Platform
import org.bundleproject.bundle.utils.API
import org.bundleproject.bundle.utils.API_VERSION
import org.bundleproject.bundle.utils.http

data class ModRequest(
    val id: String,
    val platform: Platform,
    val minecraftVersion: String,
    val version: String = "latest",
) {
    constructor(
        id: String,
        platform: String,
        minecraftVersion: String,
        version: String = "latest",
    ) : this(id, Platform.fromId(platform)!!, minecraftVersion, version)

    @Transient
    val endpoint = "$API/$API_VERSION/mods/$id/$platform/$minecraftVersion/$version"

    suspend fun request(): ModData {
        return http.get<ModResponse>(endpoint).data
    }
}

