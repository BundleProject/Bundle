package org.bundleproject.bundle.api.requests

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.ktor.client.request.*
import org.bundleproject.bundle.api.data.ModData
import org.bundleproject.bundle.api.responses.BulkModResponse
import org.bundleproject.bundle.utils.*

data class BulkModRequest(
    val mods: List<ModRequest>
) {
    suspend fun request(): List<ModData> {
        val json = JsonArray().apply {
            mods.forEach { add(gson.toJsonTree(it)) }
        }

        val base64 = gson.toJson(json).encodeBase64()
        return http.get<BulkModResponse>("$API/$API_VERSION/mods/bulk/$base64").mods
    }
}