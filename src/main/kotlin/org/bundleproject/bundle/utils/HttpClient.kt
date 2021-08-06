package org.bundleproject.bundle.utils

import com.github.zafarkhaja.semver.Version
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import org.bundleproject.bundle.utils.adapters.VersionTypeAdapter

val http = HttpClient(Apache) {
    install(JsonFeature) {
        serializer = GsonSerializer {
            registerTypeAdapter(Version::class.java, VersionTypeAdapter)
        }
    }
}